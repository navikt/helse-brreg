package no.nav.helse.brreg

internal val brregDir = "/tmp/brreg"

internal val brregJsonAlleUnderenheter = "$brregDir/underenheter_alle.json"
internal val brregJsonAlleEnheter = "$brregDir/enheter_alle.json"

private fun systemEnv(name: String) = envOrProp(name) ?: error("Mangler env var '$name'")

private fun systemEnvOrDefault(name: String, default: String) = envOrProp(name) ?: default

private fun envOrProp(name: String) = System.getenv(name) ?: System.getProperty(name.asEnvKeyToPropKey())

private fun String.asEnvKeyToPropKey() = toLowerCase().replace('_', '.')
