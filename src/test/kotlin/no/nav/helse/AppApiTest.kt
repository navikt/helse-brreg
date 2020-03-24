package no.nav.helse

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.*
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.brregModule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppApiTest {

    private val json = Json(JsonConfiguration.Stable)
    private val alleEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json")
    private val alleUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json")

    @Test
    fun `hent underenhet`() {
        withTestApplication({
            brregModule(alleEnheter, alleUnderenheter)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/underenheter/995298775").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orginfo = json.parseJson(response.content!!).jsonObject
                assertNotNull(orginfo)
                assertEquals(1005, orginfo["antallAnsatte"]!!.int)
                assertEquals("2010-04-01", orginfo["oppstartsdato"]!!.content)
                assertEquals("995298775", orginfo["organisasjonsnummer"]!!.content)
            }
        }
    }

    @Test
    fun `hent enhet`() {
        withTestApplication({
            brregModule(alleEnheter, alleUnderenheter)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/971524553").apply {
                assertTrue { response.status()?.isSuccess() ?: false }
                val orginfo = json.parseJson(response.content!!).jsonObject
                assertNotNull(orginfo)
                assertEquals(0, orginfo["antallAnsatte"]!!.int)
                assertEquals(null, orginfo["oppstartsdato"])
                assertEquals("971524553", orginfo["organisasjonsnummer"]!!.content)
            }
        }
    }

    @Test
    fun `ugyldig orgnr gir 400`() {
        withTestApplication({
            brregModule(alleEnheter, alleUnderenheter)
        }) {
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/123").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun `ikkeeksisterende orgnr gir 404`() {
        withTestApplication({
            brregModule(alleEnheter, alleUnderenheter)
        }) {
            val underenhetSomDaIkkeErEnhet = "995298775"
            handleRequest(HttpMethod.Get, "/enhetsregisteret/api/enheter/$underenhetSomDaIkkeErEnhet").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}