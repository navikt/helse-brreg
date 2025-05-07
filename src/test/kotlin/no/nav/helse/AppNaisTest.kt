package no.nav.helse

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.EnhetsregisteretOffline
import no.nav.helse.brreg.brregModule
import no.nav.helse.brreg.instrumentation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
      testApplication {
         application {
            brregModule(enhetsregisteret, false)
         }
         client.get("/isalive").apply {
            assertTrue { status.isSuccess() }
         }
      }
   }


   @Test
   fun `reports isready status for nais`() {
      testApplication {
         application {
            brregModule(enhetsregisteret, false)
         }
         client.get("/isready").apply {
            assertTrue { status.isSuccess() }
         }
      }
   }

   @Test
   fun `reports metrics`() {
      testApplication {
         application {
            brregModule(enhetsregisteret, false)
         }
         client.get("/metrics").apply {
            assertTrue { status.isSuccess() }
         }
      }
   }

}
