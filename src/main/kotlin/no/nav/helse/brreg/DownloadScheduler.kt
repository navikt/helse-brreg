package no.nav.helse.brreg

import io.prometheus.client.Summary
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private val log = LoggerFactory.getLogger("no.nav.helse.brreg.DownloadScheduler")

private val isUpdating:AtomicBoolean = AtomicBoolean(false)

private fun Long.toDate() = Date.from(Instant.ofEpochMilli(this))

internal fun setupDownloadScheduler(
    enhetsregisteretOffline: EnhetsregisteretOffline,
    downloadAndIndexTimeObserver: Summary,
    maxAgeSeconds:Long = 60L * 60 * 24
) {
    val maxAgeMillis = maxAgeSeconds * 1000L
    val lastUpdated = enhetsregisteretOffline.lastModified()
    log.info("emedded json last updated ${lastUpdated.toDate()}")
    val now = System.currentTimeMillis()

    val initialDelay = maxOf(0, (maxAgeMillis - (now - lastUpdated)))

    log.info("setting up downloadScheduler: firstRun=${(now + initialDelay).toDate()}, periodSeconds=${maxAgeMillis/1000}")
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    scheduler.scheduleAtFixedRate({
        if (!isUpdating.get()) {
            isUpdating.set(true)
            val timer = downloadAndIndexTimeObserver.startTimer()
            try {
                log.info("Oppdaterer alle_underenheter")
                enhetsregisteretOffline.erstattAlleUnderenheter(EnhetsregisterDownloader.hentAlleUnderenheter())
                log.info("Oppdaterer alle_enheter")
                enhetsregisteretOffline.erstattAlleEnheter(EnhetsregisterDownloader.hentAlleEnheter())
                log.info("oppdatert. Ny lastModified=${enhetsregisteretOffline.lastModified().toDate()}")
            } finally {
                timer.observeDuration()
                isUpdating.set(false)
            }
        }
    }, initialDelay, maxAgeMillis, TimeUnit.MILLISECONDS)
}