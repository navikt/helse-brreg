package no.nav.helse

import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.content
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.EnhetsregisteretOffline
import no.nav.helse.brreg.Instrumentation
import no.nav.helse.brreg.OrgNr
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// NB: Denne feiler med jackson 2.10.1 pga jackson-bug i parser.currentLocation.byteOffset
// ref: https://github.com/FasterXML/jackson-core/issues/603
class EnhetsregisteretOfflineTest {

    private lateinit var enhetsregisteret:EnhetsregisteretOffline

    @BeforeEach
    fun setupEach() {
        enhetsregisteret = EnhetsregisteretOffline(
            instrumentation = Instrumentation(CollectorRegistry()),
            alleEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json"),
            alleUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json"),
            slettUnderliggendeFilVedErstatt = false)
    }

    @Test
    fun `offline oppslag på underenhet skal fungere`() {
        runBlocking {
            val data = enhetsregisteret.hentUnderenhet(OrgNr("995298775"))
            assertNotNull(data)
            assertEquals("995298775", data["organisasjonsnummer"]!!.content)
        }
    }

    @Test
    fun `offline oppslag på enhet skal fungere`() {
        runBlocking {
            val data = enhetsregisteret.hentEnhet(OrgNr("971524553"))
            assertNotNull(data)
            assertEquals("971524553", data["organisasjonsnummer"]!!.content)
        }
    }

    @Test
    fun `erstatning av enheter`() {
        val andreEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_andre_enheter.json")
        enhetsregisteret.erstattAlleEnheter(andreEnheter)
        assertNull(enhetsregisteret.hentEnhet(OrgNr("971524553")))
        val data = enhetsregisteret.hentEnhet(OrgNr("958935420"))
        assertNotNull(data)
        assertEquals("958935420", data["organisasjonsnummer"]!!.content)
    }

    @Test
    fun `erstatning av underenheter`() {
        val andreUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_andre_underenheter.json")
        enhetsregisteret.erstattAlleUnderenheter(andreUnderenheter)
        assertNull(enhetsregisteret.hentUnderenhet(OrgNr("995298775")))
        val data = enhetsregisteret.hentUnderenhet(OrgNr("974486725"))
        assertNotNull(data)
        assertEquals("974486725", data["organisasjonsnummer"]!!.content)
    }
}