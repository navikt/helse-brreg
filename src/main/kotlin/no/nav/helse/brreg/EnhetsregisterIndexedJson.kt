package no.nav.helse.brreg

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

private val log = LoggerFactory.getLogger(EnhetsregisterIndexedJson::class.java)

class EnhetsregisterIndexedJson(
    private val jsonfilnavn: String
) {

    val lastModified: Long
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    private val index: MutableMap<String, Pair<Long, Long>>
    private val overordnetTilUnderordnet: MutableMap<String, MutableList<String>>

    init {
        lastModified = File(jsonfilnavn).lastModified()
        createIndex().also {
            index = it.orgnrToPosition
            overordnetTilUnderordnet = it.overordnetToUnderenheter
        }
    }

    fun size() = index.size

    fun lookupOrg(orgNr: OrgNr): JsonObject? {
        val pos = index[orgNr.value] ?: return null
        RandomAccessFile(jsonfilnavn, "r").use { file ->
            file.seek(pos.first)
            val bytes = ByteArray(pos.second.toInt())
            file.read(bytes)
            val jsonData = String(bytes, Charset.forName("UTF-8"))
            return json.parseToJsonElement(jsonData).jsonObject
        }
    }

    fun lookupOrgnrMedOverordnetOrgnr(orgNr: OrgNr): List<String> =
        overordnetTilUnderordnet[orgNr.value] ?: emptyList()


    internal fun deleteUnderlyingFile() {
        log.info("deleting $jsonfilnavn")
        index.clear()
        overordnetTilUnderordnet.clear()
        try {
            Files.delete(Path.of(jsonfilnavn))
            log.info("deleted $jsonfilnavn")
        } catch (ex: IOException) {
            log.warn("unable to delete $jsonfilnavn", ex)
        }
    }

    enum class MyState {
        EXPECT_OBJECT,
        EXPECT_ORGNO,
        EXPECT_OVERORDNET
    }

    private data class Indexes(
        val orgnrToPosition: MutableMap<String, Pair<Long, Long>>,
        val overordnetToUnderenheter: MutableMap<String, MutableList<String>>
    )

    private fun createIndex(): Indexes {
        val jfactory = JsonFactory()
        val orgnrToPosition = mutableMapOf<String, Pair<Long, Long>>()
        val overordnetToUnderenheter = mutableMapOf<String, MutableList<String>>()
        var utenOverordnetCount = 0

        var count = 0
        jfactory.createParser(File(jsonfilnavn)).use { parser ->
            var token: JsonToken = JsonToken.VALUE_NULL
            var obj = 0
            var state = MyState.EXPECT_OBJECT
            var objectPos = 0L
            var orgnr = ""
            var overordnet: String? = null

            while ((parser.nextToken()?.apply { token = this }) != null) {
                when (token) {
                    JsonToken.START_OBJECT -> {
                        obj += 1
                        if (1 == obj) objectPos = parser.currentLocation.byteOffset - 1
                    }
                    JsonToken.END_OBJECT -> {
                        obj -= 1
                        if (0 == obj) {
                            count += 1
                            if (overordnet != null) {
                                overordnetToUnderenheter
                                    .getOrPut(overordnet, { mutableListOf() })
                                    .add(orgnr)
                            } else {
                                utenOverordnetCount += 1
                            }
                            orgnrToPosition.put(
                                orgnr,
                                objectPos to (parser.currentLocation.byteOffset - objectPos)
                            )
                            if (count.rem(100000) == 0)
                                log.info("$count enheter indeksert")
                        }
                    }
                    JsonToken.FIELD_NAME -> if (1 == obj) {
                        if (parser.currentName == "organisasjonsnummer")
                            state = MyState.EXPECT_ORGNO
                        else if (parser.currentName == "overordnetEnhet")
                            state = MyState.EXPECT_OVERORDNET
                    }
                    JsonToken.VALUE_NULL,
                    JsonToken.VALUE_STRING ->
                        if (state == MyState.EXPECT_ORGNO) {
                            orgnr = parser.valueAsString
                            state = MyState.EXPECT_OBJECT
                        } else if (state == MyState.EXPECT_OVERORDNET) {
                            overordnet = parser.valueAsString
                            state = MyState.EXPECT_OBJECT
                        }
                    else -> Unit
                }
            }
            log.info("$count enheter indeksert")
            if (count == 0) {
                log.error("$count enheter indeksert, noe er galt.")
                throw RuntimeException("$count enheter indeksert, noe er galt.")
            }
        }
        log.info("overordnetToUnderenheter.size=${overordnetToUnderenheter.size}")
        log.info("utenOverordnetCount=${utenOverordnetCount}")
        overordnetToUnderenheter.entries.filter { it.value.size > 1 }.let { medFlereUnderenheter ->
            val overSize = medFlereUnderenheter.size
            log.info(">1 over-size=${overSize}")
            val underSize = medFlereUnderenheter.flatMap { it.value }.size
            log.info(">1 under-size=${underSize}")
            val totalBørVære = overordnetToUnderenheter.size - overSize + underSize + utenOverordnetCount
            log.info("totalBørVære $totalBørVære")
            if (totalBørVære != count) {
                throw RuntimeException("Konsistensfeil: $totalBørVære != $count")
            }
        }
        return Indexes(orgnrToPosition, overordnetToUnderenheter)
    }
}
