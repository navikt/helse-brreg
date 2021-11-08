package no.nav.helse.brreg

import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.*

private val log = LoggerFactory.getLogger(EnhetsregisterDownloader::class.java)

class EnhetsregisterDownloader {
    companion object {
        fun hentAlleEnheter() = createIndexedJsonFromUrl(alleEnheterUrl, "enheter_alle_")
        fun hentAlleUnderenheter() = createIndexedJsonFromUrl(alleUnderEnheterUrl, "underenheter_alle_")

        private val alleEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/enheter/lastned")
        private val alleUnderEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/underenheter/lastned")

        private val dir = "/tmp/brreg"

        private fun createIndexedJsonFromUrl(url: URL, prefix:String) : EnhetsregisterIndexedJson{
            return try {
                val filenameJson = "$dir/$prefix${UUID.randomUUID()}.json"
                val filenameGZIP = "$$filenameJson.gz"
                log.info("laster ned fra $url")

                url.openStream().use { urlStream ->
                    Channels.newChannel(urlStream).use {byteChannel ->
                        FileOutputStream(filenameGZIP).use {
                            it.channel.transferFrom(byteChannel, 0, Long.MAX_VALUE)
                        }
                    }
                }

                log.info("pakker ut $filenameGZIP")
                ProcessBuilder()
                    .command("gunzip", filenameGZIP)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor()
                log.info("touching $filenameJson")
                ProcessBuilder()
                    .command("touch", filenameJson)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor()
                log.info("laster og indekserer $filenameJson")
                EnhetsregisterIndexedJson(filenameJson)
            } catch (ex:Throwable) {
                log.error("Feil under nedlasting: ", ex)
                throw ex
            }
        }
    }
}