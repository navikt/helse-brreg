package no.nav.helse.brreg

import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.LoggerFactory
import java.io.StringWriter

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val collectorRegistry = CollectorRegistry.defaultRegistry
internal val instrumentation = Instrumentation(collectorRegistry)

private val log = LoggerFactory.getLogger("no.nav.helse.brreg.Application")

fun Application.brregModule(
    enhetsregisteret: EnhetsregisteretOffline =
        EnhetsregisteretOffline(
            instrumentation = instrumentation,
            alleEnheter = EnhetsregisterIndexedJson(brregEmbeddedJsonAlleEnheter),
            alleUnderenheter = EnhetsregisterIndexedJson(brregEmbeddedJsonAlleUnderenheter)
        ),
    enableDownloadScheduler: Boolean = true
) {
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            collectorRegistry,
            Clock.SYSTEM
        )
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics()
        )
    }

    if (enableDownloadScheduler) setupDownloadScheduler(enhetsregisteret, instrumentation.downloadAndIndexTimeObserver)

    routing {
        get("/enhetsregisteret/api/underenheter/{orgnr}") {
            log.info("get underenheter")
            val orgnr = try {
                OrgNr(call.parameters["orgnr"]!!)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = enhetsregisteret.hentUnderenhet(orgnr)
            if (null == data) {
                call.respond(HttpStatusCode.NotFound, "fant ikke organisasjon")
                return@get
            }
            call.respondText(data.toString(),
                ContentType("application", "json"))
        }

        get("/enhetsregisteret/api/enheter/{orgnr}") {
            log.info("get enheter")
            val orgnr = try {
                OrgNr(call.parameters["orgnr"]!!)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = enhetsregisteret.hentEnhet(orgnr)
            if (null == data) {
                call.respond(HttpStatusCode.NotFound, "fant ikke organisasjon")
                return@get
            }
            call.respondText(data.toString(),
                ContentType("application", "json"))
        }

        get("/enhetsregisteret/api/underenheter_for_overordnet/{overordnet_orgnr}") {
            log.info("get underenheter_for_overordnet")
            val orgnr = try {
                OrgNr(call.parameters["overordnet_orgnr"]!!)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = enhetsregisteret.hentUnderenheterHvorOverordnetEr(orgnr)
            call.respondText(data.toString(),
                ContentType("application", "json"))
        }

        get("/enhetsregisteret/api/enheter_for_overordnet/{overordnet_orgnr}") {
            log.info("get enheter_for_overordnet")
            val orgnr = try {
                OrgNr(call.parameters["overordnet_orgnr"]!!)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = enhetsregisteret.hentEnheterHvorOverordnetEr(orgnr)
            call.respondText(data.toString(),
                ContentType("application", "json"))
        }

        get("/isalive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/isready") {
            call.respondText("READY", ContentType.Text.Plain)
        }
        get("/metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
            val text = StringWriter()
            TextFormat.write004(text, collectorRegistry.filteredMetricFamilySamples(names))
            call.respondText(text = text.toString(), contentType = ContentType.parse(TextFormat.CONTENT_TYPE_004))
        }
    }
}

