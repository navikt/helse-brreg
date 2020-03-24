package no.nav.helse

import no.nav.helse.brreg.OrgNr
import org.junit.jupiter.api.*

class OrgNrTest {

   @Test
   fun `riktig sjekksum er ok`() {
      assertDoesNotThrow { OrgNr("123456785") }
   }

   @Test
   fun `feil sjekksum avvises`() {
      assertThrows<IllegalArgumentException> { OrgNr("123456786") }
   }

}
