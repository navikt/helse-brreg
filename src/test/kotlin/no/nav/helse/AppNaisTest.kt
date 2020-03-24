package no.nav.helse

import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.EnhetsregisteretOffline
import no.nav.helse.brreg.brregModule
import no.nav.helse.brreg.instrumentation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@io.ktor.util.KtorExperimentalAPI
class AppNaisTest {

   private val alleEnheter = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json")
   private val alleUnderenheter = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json")
   private val enhetsregisteret = EnhetsregisteretOffline(
       instrumentation = instrumentation,
       alleEnheter = alleEnheter,
       alleUnderenheter = alleUnderenheter,
       slettUnderliggendeFilVedErstatt = false
   )
   @Test
   fun `reports isalive status for nais`() {
      withTestApplication({
         brregModule(enhetsregisteret)
      }) {
         handleRequest(HttpMethod.Get, "/isalive").apply {
            assertTrue { response.status()?.isSuccess() ?: false }
         }
      }

   }

   @Test
   fun `reports isready status for nais`() {
      withTestApplication({
         brregModule(enhetsregisteret)
      }) {
         handleRequest(HttpMethod.Get, "/isready").apply {
            assertTrue { response.status()?.isSuccess() ?: false }
         }
      }

   }

   @Test
   fun `reports metrics`() {
      withTestApplication({
         brregModule(enhetsregisteret)
      }) {
         handleRequest(HttpMethod.Get, "/metrics").apply {
            assertTrue { response.status()?.isSuccess() ?: false }
         }
      }

   }

}
