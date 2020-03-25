package no.nav.helse.brreg

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private val log = LoggerFactory.getLogger("no.nav.helse.brreg.DownloadScheduler")

private val isUpdating:AtomicBoolean = AtomicBoolean(false)

internal fun setupDownloadScheduler(
    enhetsregisteretOffline: EnhetsregisteretOffline,
    maxAgeSeconds:Long = 5* 60L
) {
    val maxAgeMillis = maxAgeSeconds * 1000L
    val lastUpdated = enhetsregisteretOffline.lastModified()
    log.info("emedded json last updated ${Date.from(Instant.ofEpochMilli(lastUpdated))}")
    val now = System.currentTimeMillis()

    val initialDelay = maxOf(0, (maxAgeMillis - (now - lastUpdated)))

    log.info("setting up scheduler: initialDelay=$initialDelay, period=$maxAgeMillis")
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    scheduler.scheduleAtFixedRate({
        if (!isUpdating.get()) {
            isUpdating.set(true)
            try {
                log.info("Oppdaterer alle_enheter")
                enhetsregisteretOffline.erstattAlleUnderenheter(EnhetsregisterDownloader.hentAlleUnderenheter())
                log.info("Oppdaterer alle_underenheter")
                enhetsregisteretOffline.erstattAlleEnheter(EnhetsregisterDownloader.hentAlleEnheter())
                log.info("oppdatert. Ny lastModified=${enhetsregisteretOffline.lastModified()}")
            } finally {
                isUpdating.set(false)
            }
        }
    }, initialDelay, maxAgeMillis, TimeUnit.MILLISECONDS)
}