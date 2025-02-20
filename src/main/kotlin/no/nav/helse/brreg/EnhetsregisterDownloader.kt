package no.nav.helse.brreg

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.zip.GZIPInputStream

private val log = LoggerFactory.getLogger(EnhetsregisterDownloader::class.java)

internal fun decompressGzipFile(gzipFile: String, newFile: String) {
    val bufferSize = 64 * 1024
    GZIPInputStream(FileInputStream(gzipFile), bufferSize).use { gis ->
        FileOutputStream(newFile).use { fos ->
            val buffer = ByteArray(bufferSize)
            var len: Int
            while ((gis.read(buffer).also { len = it }) != -1) {
                fos.write(buffer, 0, len)
            }
        }
    }
}

class EnhetsregisterDownloader {
    companion object {
        fun hentAlleEnheter() = createIndexedJsonFromUrl(alleEnheterUrl, "enheter_alle_")
        fun hentAlleUnderenheter() = createIndexedJsonFromUrl(alleUnderEnheterUrl, "underenheter_alle_")

        private val alleEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/enheter/lastned")
        private val alleUnderEnheterUrl = URL("https://data.brreg.no/enhetsregisteret/api/underenheter/lastned")

        private val dir = embeddedBrregDir

        private fun createIndexedJsonFromUrl(url: URL, prefix:String) : EnhetsregisterIndexedJson{
            return try {
                val filenameJson = "$dir/$prefix${UUID.randomUUID()}.json"
                val filenameGZIP = "$filenameJson.gz"
                log.info("laster ned fra $url")

                url.openStream().use { urlStream ->
                    Channels.newChannel(urlStream).use {byteChannel ->
                        FileOutputStream(filenameGZIP).use {
                            it.channel.transferFrom(byteChannel, 0, Long.MAX_VALUE)
                        }
                    }
                }

                log.info("pakker ut $filenameGZIP til $filenameJson")
                decompressGzipFile(filenameGZIP, filenameJson)
                log.info("sletter $filenameGZIP")
                Files.delete(Path.of(filenameGZIP))
                log.info("laster og indekserer $filenameJson")
                EnhetsregisterIndexedJson(filenameJson)
            } catch (ex:Throwable) {
                log.error("Feil under nedlasting: ", ex)
                throw ex
            }
        }
    }
}