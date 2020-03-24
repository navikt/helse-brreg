package no.nav.helse.brreg

import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(EnhetsregisteretOffline::class.java)

class EnhetsregisteretOffline(
    private val instrumentation: Instrumentation,
    private val alleEnheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleEnheter),
    private val alleUnderenheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleUnderenheter)
) {
    fun lookupOrg(orgNr: OrgNr): JsonObject? {
        val info = alleUnderenheter.lookupOrg(orgNr) ?: alleEnheter.lookupOrg(orgNr)
        return if (info != null) {
            instrumentation.lookupSucceeded()
            return info
        } else {
            log.info("Fant ikke organisasjon: ${orgNr.value}")
            instrumentation.lookupFailed()
            null
        }
    }
}