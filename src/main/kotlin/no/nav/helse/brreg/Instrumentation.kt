package no.nav.helse.brreg

import io.prometheus.client.*

class Instrumentation(registry: CollectorRegistry) {

   private val lookupEnhetCounter = Counter
      .build("helseBrregLookupEnhet", "nr of organization lookups")
      .labelNames("outcome")
      .register(registry)

   private val lookupUnderenhetCounter = Counter
       .build("helseBrregLookupUnderenhet", "nr of organization lookups")
       .labelNames("outcome")
       .register(registry)

   private val erstattEnhetsregisterCounter = Counter
       .build("helseBrregErstattEnhetsregister", "nr of organization lookups")
       .labelNames("erstattet")
       .register(registry)

   private val topLevelExceptionCounter = Counter
      .build("uncaught", "nr of uncaught exceptions")
      .register(registry)

   internal val downloadAndIndexTimeObserver = Summary.build("helseBrregDownloadAndIndex", "download and reindexing time for update in seconds")
       .register(registry)


   fun lookupEnhetSucceeded() {
      lookupEnhetCounter.labels("success").inc()
   }

   fun lookupEnhetFailed() {
      lookupEnhetCounter.labels("failure").inc()
   }

   fun lookupUnderenhetSucceeded() {
      lookupUnderenhetCounter.labels("success").inc()
   }

   fun lookupUnderenhetFailed() {
      lookupUnderenhetCounter.labels("failure").inc()
   }

   fun erstattetUnderenheter() {
      erstattEnhetsregisterCounter.labels("underenheter").inc()
   }

   fun erstattetEnheter() {
      erstattEnhetsregisterCounter.labels("enheter").inc()
   }

   fun topLevelError() {
      topLevelExceptionCounter.inc()
   }

}
