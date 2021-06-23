package no.nav.helse

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.*
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.EnhetsregisteretOffline
import no.nav.helse.brreg.brregModule
import no.nav.helse.brreg.instrumentation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppApiTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    private val alleEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json")
    private val alleUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json")
    private val enhetsregisteret = EnhetsregisteretOffline(
        instrumentation = instrumentation,
        alleEnheter = alleEnheter,
        alleUnderenheter = alleUnderenheter,
        slettUnderliggendeFilVedErstatt = false
    )

    @Test
    fun `hent underenhet`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/underenheter/995298775").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orginfo = json.parseToJsonElement(response.content!!).jsonObject
                assertNotNull(orginfo)
                assertEquals(1005, orginfo["antallAnsatte"]!!.jsonPrimitive.int)
                assertEquals("2010-04-01", orginfo["oppstartsdato"]!!.jsonPrimitive.content)
                assertEquals("995298775", orginfo["organisasjonsnummer"]!!.jsonPrimitive.content)
            }
        }
    }

    @Test
    fun `hent enhet`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/971524553").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orginfo = json.parseToJsonElement(response.content!!).jsonObject
                assertNotNull(orginfo)
                assertEquals(0, orginfo["antallAnsatte"]!!.jsonPrimitive.int)
                assertEquals(null, orginfo["oppstartsdato"])
                assertEquals("971524553", orginfo["organisasjonsnummer"]!!.jsonPrimitive.content)
            }
        }
    }

    @Test
    fun `hent underenheter med overordnet`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/underenheter_for_overordnet/889640782").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orglist = json.parseToJsonElement(response.content!!).jsonArray
                assertEquals(
                    setOf("995298775", "995298776"),
                    orglist.map { it.jsonPrimitive.content }.toSet())
            }
        }
    }

    @Test
    fun `hent enheter med overordnet`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter_for_overordnet/991012206").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orglist = json.parseToJsonElement(response.content!!).jsonArray
                assertEquals(
                    setOf("971524553", "888777666"),
                    orglist.map { it.jsonPrimitive.content }.toSet())
            }
        }
    }

    @Test
    fun `ugyldig orgnr gir 400`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/123").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun `ikkeeksisterende orgnr gir 404`() {
        withTestApplication({
            brregModule(enhetsregisteret, false)
        }) {
            val underenhetSomDaIkkeErEnhet = "995298775"
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/$underenhetSomDaIkkeErEnhet").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}