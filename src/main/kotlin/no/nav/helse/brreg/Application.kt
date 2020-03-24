package no.nav.helse.brreg

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.*
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.*
import java.lang.IllegalArgumentException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val log = LoggerFactory.getLogger("no.nav.helse.brreg.Application")

private val collectorRegistry = CollectorRegistry.defaultRegistry
private val instrumentation = Instrumentation(collectorRegistry)

fun Application.brregModule(
    alleEnheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleEnheter),
    alleUnderenheter: EnhetsregisterIndexedJson = EnhetsregisterIndexedJson(brregJsonAlleUnderenheter)
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

    routing {


        get("/enhetsregisteret/api/underenheter/{orgnr}") {
            val orgnr = try {
                OrgNr(call.parameters["orgnr"]!!)
            } catch (ex:IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = alleUnderenheter.lookupOrg(orgnr)
            if (null == data) {
                call.respond(HttpStatusCode.NotFound, "fant ikke organisasjon")
                return@get
            }
            call.respondText(data.toString(),
                ContentType("application", "json"))
        }

        get("/enhetsregisteret/api/enheter/{orgnr}") {
            val orgnr = try {
                OrgNr(call.parameters["orgnr"]!!)
            } catch (ex:IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "ugyldig orgnr")
                return@get
            }
            val data = alleEnheter.lookupOrg(orgnr)
            if (null == data) {
                call.respond(HttpStatusCode.NotFound, "fant ikke organisasjon")
                return@get
            }
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
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
            }
        }
    }
}

