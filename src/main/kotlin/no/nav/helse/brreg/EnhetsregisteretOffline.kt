package no.nav.helse.brreg

import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(EnhetsregisteretOffline::class.java)

class EnhetsregisteretOffline(
    private val instrumentation: Instrumentation,
    private var alleEnheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleEnheter),
    private var alleUnderenheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleUnderenheter),
    private val slettUnderliggendeFilVedErstatt: Boolean = true
) {
    fun hentEnhet(orgNr: OrgNr): JsonObject? = alleEnheter.lookupOrg(orgNr)

    fun hentUnderenhet(orgNr: OrgNr): JsonObject? = alleUnderenheter.lookupOrg(orgNr)

    fun lastModified() = minOf(alleEnheter.lastModified, alleUnderenheter.lastModified)

    fun erstattAlleEnheter(nyAlleEnheter: EnhetsregisterIndexedJson) {
        val forrige = alleEnheter
        alleEnheter = nyAlleEnheter
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
    }

    fun erstattAlleUnderenheter(nyAlleUnderenheter: EnhetsregisterIndexedJson) {
        val forrige = alleUnderenheter
        alleUnderenheter = nyAlleUnderenheter
        if (slettUnderliggendeFilVedErstatt) forrige.deleteUnderlyingFile()
    }
}