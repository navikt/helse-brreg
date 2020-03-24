package no.nav.helse

import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.content
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.EnhetsregisteretOffline
import no.nav.helse.brreg.Instrumentation
import no.nav.helse.brreg.OrgNr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// NB: Denne feiler med jackson 2.10.1 pga jackson-bug i parser.currentLocation.byteOffset
// ref: https://github.com/FasterXML/jackson-core/issues/603
class EnhetsregisteretOfflineTest {

    private val enhetsregisteret =
        EnhetsregisteretOffline(
            instrumentation = Instrumentation(CollectorRegistry()),
            alleEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json"),
            alleUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json"))

    @Test
    fun `offline oppslag skal fungere`() {
        runBlocking {
            val data = enhetsregisteret.lookupOrg(OrgNr("995298775"))
            assertNotNull(data)
            assertEquals("995298775", data["organisasjonsnummer"]!!.content)
        }

    }
}