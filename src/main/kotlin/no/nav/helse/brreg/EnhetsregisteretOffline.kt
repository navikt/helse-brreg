package no.nav.helse.brreg

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(EnhetsregisteretOffline::class.java)

class EnhetsregisteretOffline(
    private val instrumentation: Instrumentation,
    private var alleEnheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregEmbeddedJsonAlleEnheter),
    private var alleUnderenheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregEmbeddedJsonAlleUnderenheter),
    private val slettUnderliggendeFilVedErstatt: Boolean = true
) {
    fun hentEnhet(orgNr: OrgNr): JsonObject? = alleEnheter.lookupOrg(orgNr).apply {
        if (this != null) instrumentation.lookupEnhetSucceeded() else instrumentation.lookupEnhetFailed()
    }

    fun hentUnderenhet(orgNr: OrgNr): JsonObject? = alleUnderenheter.lookupOrg(orgNr).apply {
        if (this != null) instrumentation.lookupUnderenhetSucceeded() else instrumentation.lookupUnderenhetFailed()
    }

    fun hentEnheterHvorOverordnetEr(orgNr: OrgNr): JsonArray =
        alleEnheter.lookupOrgnrMedOverordnetOrgnr(orgNr).let { list ->
            buildJsonArray { list.forEach { add(JsonPrimitive(it)) }  }
        }

    fun hentUnderenheterHvorOverordnetEr(orgNr: OrgNr): JsonArray =
        alleUnderenheter.lookupOrgnrMedOverordnetOrgnr(orgNr).let { list ->
            buildJsonArray { list.forEach { add(JsonPrimitive(it)) }  }
        }

    fun lastModified() = minOf(alleEnheter.lastModified, alleUnderenheter.lastModified)

    fun erstattAlleEnheter(nyAlleEnheter: EnhetsregisterIndexedJson) {
        val forrige = alleEnheter
        alleEnheter = nyAlleEnheter
        log.info("Erstattet alle enheter. oldSize=${forrige.size()} newSize=${alleUnderenheter.size()}")
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
        instrumentation.erstattetEnheter()
    }

    fun erstattAlleUnderenheter(nyAlleUnderenheter: EnhetsregisterIndexedJson) {
        val forrige = alleUnderenheter
        alleUnderenheter = nyAlleUnderenheter
        log.info("Erstattet alle underenheter. oldSize=${forrige.size()} newSize=${alleUnderenheter.size()}")
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
        instrumentation.erstattetUnderenheter()
    }
}