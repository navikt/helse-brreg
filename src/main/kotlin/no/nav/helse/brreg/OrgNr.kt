package no.nav.helse.brreg

class OrgNr(val value: String) {
   private val weights = listOf(3, 2, 7, 6, 5, 4, 3, 2)

   init {
      if (!checksumValid()) {
         throw IllegalArgumentException("Sjekksum stemmer ikke")
      }
   }

   private fun checksumValid(): Boolean {
      if (value.length != 9) return false
      val weightedSum = (0..7)
         .map { idx ->  Character.getNumericValue(value[idx]) * weights[idx]}
         .sum()

      val controlDigit = when (val remainder = weightedSum.rem(11)) {
         0 -> 0
         1 -> -10
         else -> 11 - remainder
      }

      return controlDigit == Character.getNumericValue(value[8])
   }

}
