package no.nav.helse.brreg

import kotlinx.serialization.json.JsonObject

class EnhetsregisteretOffline(
    private val instrumentation: Instrumentation,
    private var alleEnheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleEnheter),
    private var alleUnderenheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleUnderenheter),
    private val slettUnderliggendeFilVedErstatt: Boolean = true
) {
    fun hentEnhet(orgNr: OrgNr): JsonObject? = alleEnheter.lookupOrg(orgNr).apply {
        if (this != null) instrumentation.lookupEnhetSucceeded() else instrumentation.lookupEnhetFailed()
    }

    fun hentUnderenhet(orgNr: OrgNr): JsonObject? = alleUnderenheter.lookupOrg(orgNr).apply {
        if (this != null) instrumentation.lookupUnderenhetSucceeded() else instrumentation.lookupUnderenhetFailed()
    }

    fun lastModified() = minOf(alleEnheter.lastModified, alleUnderenheter.lastModified)

    fun erstattAlleEnheter(nyAlleEnheter: EnhetsregisterIndexedJson) {
        val forrige = alleEnheter
        alleEnheter = nyAlleEnheter
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
        instrumentation.erstattetEnheter()
    }

    fun erstattAlleUnderenheter(nyAlleUnderenheter: EnhetsregisterIndexedJson) {
        val forrige = alleUnderenheter
        alleUnderenheter = nyAlleUnderenheter
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
        instrumentation.erstattetUnderenheter()
    }
}