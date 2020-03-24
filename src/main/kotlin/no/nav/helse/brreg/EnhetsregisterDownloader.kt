package no.nav.helse.brreg

import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.*

fun main() {
    val enheter = EnhetsregisterDownloader.hentAlleEnheter()
    Thread.sleep(10000)
    enheter.deleteUnderlyingFile()
}

private val log = LoggerFactory.getLogger(EnhetsregisterDownloader::class.java)

class EnhetsregisterDownloader {
    companion object {
        fun hentAlleEnheter() = createIndexedJsonFromUrl(alleEnheterUrl, "enheter_alle_")
        fun hentAlleUnderenheter() = createIndexedJsonFromUrl(alleUnderEnheterUrl, "underenheter_alle_")

        private val alleEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/enheter/lastned")
        private val alleUnderEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/underenheter/lastned")

        private val dir = "."

        private fun createIndexedJsonFromUrl(url: URL, prefix:String) : EnhetsregisterIndexedJson{
            val filenameJson = "$dir/$prefix${UUID.randomUUID()}.json"
            val filenameGZIP = "$filenameJson.gz"
            log.info("laster ned fra $url")
            val readableByteChannel: ReadableByteChannel = Channels.newChannel(url.openStream())
            FileOutputStream(filenameGZIP).use {
                it.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
            }
            log.info("pakker ut $filenameGZIP")
            ProcessBuilder()
                .command("gunzip", filenameGZIP)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()
            log.info("laster og indekser $filenameJson")
            return EnhetsregisterIndexedJson(filenameJson)
        }
    }
}