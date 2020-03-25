package no.nav.helse.brreg

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
    maxAgeSeconds:Long = 5* 60L
) {
    val maxAgeMillis = maxAgeSeconds * 1000L
    val lastUpdated = enhetsregisteretOffline.lastModified()
    log.info("emedded json last updated ${lastUpdated.toDate()}")
    val now = System.currentTimeMillis()

    val initialDelay = maxOf(0, (maxAgeMillis - (now - lastUpdated)))

    log.info("setting up scheduler: initialDelaySecs=${initialDelay/1000}, periodSecs=${maxAgeMillis/1000}")
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    scheduler.scheduleAtFixedRate({
        if (!isUpdating.get()) {
            isUpdating.set(true)
            try {
                log.info("Oppdaterer alle_underenheter")
                enhetsregisteretOffline.erstattAlleUnderenheter(EnhetsregisterDownloader.hentAlleUnderenheter())
                log.info("Oppdaterer alle_enheter")
                enhetsregisteretOffline.erstattAlleEnheter(EnhetsregisterDownloader.hentAlleEnheter())
                log.info("oppdatert. Ny lastModified=${enhetsregisteretOffline.lastModified().toDate()}")
            } finally {
                isUpdating.set(false)
            }
        }
    }, initialDelay, maxAgeMillis, TimeUnit.MILLISECONDS)
}