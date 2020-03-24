package no.nav.helse

import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.OrgNr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EnhetsregisterIndexedJsonTest {

    val brreg = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json")

    @Test
    fun `offline oppslag skal fungere`() {
        val orginfo = brreg.lookupOrg(OrgNr("974508737"))
        assertNotNull(orginfo)
        assertEquals(2, orginfo["antallAnsatte"]!!.int)
        assertEquals("1995-05-23", orginfo["oppstartsdato"]!!.content)
        assertEquals("974508737", orginfo["organisasjonsnummer"]!!.content)
    }

}