package no.nav.helse.brreg

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

fun main() {
   /*val dataPath = "${System.getenv("HOME")}/Downloads/underenheter_alle.json"
   val service = EnhetsregisterIndexedJson(dataPath)
   println(service.lookupOrg(OrgNr("995298775")))*/

    val dataPath = "${System.getenv("HOME")}/Downloads/enheter_alle.json"
    val service = EnhetsregisterIndexedJson(dataPath)
    println(service.lookupOrg(OrgNr("971524553")))
}

private val log = LoggerFactory.getLogger(EnhetsregisterIndexedJson::class.java)

class EnhetsregisterIndexedJson(
   private val jsonfilnavn:String) {

   val lastModified:Long
   private val json = Json(JsonConfiguration.Stable)
   private val index: Map<String, Pair<Long, Long>>

   init {
      lastModified = File(jsonfilnavn).lastModified()
      index = createIndex()
   }

   fun lookupOrg(orgNr: OrgNr): JsonObject? {
      val pos = index[orgNr.value] ?: return null
      RandomAccessFile(jsonfilnavn, "r").use { file ->
         file.seek(pos.first)
         val bytes = ByteArray(pos.second.toInt())
         file.read(bytes)
         val jsonData = String(bytes, Charset.forName("UTF-8"))
         return json.parseJson(jsonData).jsonObject
      }
   }

   internal fun deleteUnderlyingFile() {
      log.info("deleting $jsonfilnavn")
      try {
         Files.delete(Path.of(jsonfilnavn))
         log.info("deleted $jsonfilnavn")
      } catch (ex: IOException) {
         log.warn("unable to delete $jsonfilnavn", ex)
      }
   }

   enum class MyState {
      EXPECT_OBJECT,
      EXPECT_ORGNO
   }

   private fun createIndex() : Map<String, Pair<Long, Long>> {
      val jfactory = JsonFactory()
      val orgnrToPosition = mutableMapOf<String, Pair<Long, Long>>()
      jfactory.createParser(File(jsonfilnavn)).use { parser ->
         var token: JsonToken = JsonToken.VALUE_NULL
         var obj = 0
         var state = MyState.EXPECT_OBJECT
         var objectPos = 0L
         var orgnr = ""
         var count = 0
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
                     orgnrToPosition.put(
                        orgnr,
                        objectPos to (parser.currentLocation.byteOffset - objectPos)
                     )
                     if (count.rem(100000) == 0)
                        log.info("$count enheter indeksert")
                  }
               }
               JsonToken.FIELD_NAME -> if ((1 == obj) && (parser.currentName == "organisasjonsnummer"))
                  state = MyState.EXPECT_ORGNO
               JsonToken.VALUE_STRING -> if (state == MyState.EXPECT_ORGNO) {
                  orgnr = parser.valueAsString
                  state = MyState.EXPECT_OBJECT
               }
               else -> Unit
            }
         }
         log.info("$count enheter indeksert")
      }
      return orgnrToPosition
   }
}
