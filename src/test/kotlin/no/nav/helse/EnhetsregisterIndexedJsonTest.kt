package no.nav.helse

import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import no.nav.helse.brreg.EnhetsregisterIndexedJson
import no.nav.helse.brreg.OrgNr
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EnhetsregisterIndexedJsonTest {

    val brreg = EnhetsregisterIndexedJson("./src/test/resources/noen_underenheter.json")
    val brregOverordnet = EnhetsregisterIndexedJson("./src/test/resources/noen_enheter.json")

    @Test
    fun `offline oppslag skal fungere`() {
        val orginfo = brreg.lookupOrg(OrgNr("995298775"))
        assertNotNull(orginfo)
        assertEquals(1005, orginfo["antallAnsatte"]!!.jsonPrimitive.int)
        assertEquals("2010-04-01", orginfo["oppstartsdato"]!!.jsonPrimitive.content)
        assertEquals("995298775", orginfo["organisasjonsnummer"]!!.jsonPrimitive.content)
        val overordnet = orginfo["overordnetEnhet"]!!.jsonPrimitive.content
        assertEquals(
            setOf("995298775", "995298776"),
            brreg.lookupOrgnrMedOverordnetOrgnr(OrgNr(overordnet)).toSet()
        )
    }

    @Test
    @Disabled // Bruk for å manuelt teste mot hele datasettet
    fun `test med alle`() {
        val brregAlle = EnhetsregisterIndexedJson("/Users/g153965/Downloads/underenheter_alle.json")
        val brregOverordnetAlle = EnhetsregisterIndexedJson("/Users/g153965/Downloads/enheter_alle.json")

        val orginfo = brregAlle.lookupOrg(OrgNr("972321850"))
        assertNotNull(orginfo)
        assertEquals("972321850", orginfo["organisasjonsnummer"]!!.jsonPrimitive.content)
        val overordnet = orginfo["overordnetEnhet"]!!.jsonPrimitive.content
        val alleSøstre = brregAlle.lookupOrgnrMedOverordnetOrgnr(OrgNr(overordnet))
        assertEquals(
            setOf("894981512", "994981455", "994981676", "994981803", "972321850", "994982044"),
            alleSøstre.toSet()
        )

    }

}
