package no.nav.helse.brreg

import io.prometheus.client.*

class Instrumentation(registry: CollectorRegistry) {

   private val lookupCounter = Counter
      .build("orgInfoLookup", "nr of organization lookups")
      .labelNames("outcome")
      .register(registry)

   private val topLevelExceptionCounter = Counter
      .build("uncaught", "nr of uncaught exceptions")
      .register(registry)

   fun lookupSucceeded() {
      lookupCounter.labels("success").inc()
   }

   fun lookupFailed() {
      lookupCounter.labels("failure").inc()
   }

   fun topLevelError() {
      topLevelExceptionCounter.inc()
   }

}
