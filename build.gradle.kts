import org.jetbrains.kotlin.gradle.tasks.*

val junitJupiterVersion = "5.6.0"
val ktorVersion = "1.3.2"
val micrometerVersion = "1.3.3"
val slf4jVersion = "1.7.30"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "6.3"
val serializerVersion = "0.20.0"
val jacksonVersion = "2.10.3"

group = "no.nav.helse"

plugins {
   val kotlinVersion = "1.3.70"
   kotlin("jvm") version kotlinVersion
   kotlin("plugin.serialization") version kotlinVersion
   application
}

repositories {
   jcenter()
   mavenCentral()
   maven("http://packages.confluent.io/maven/")
}

dependencies {
   implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

   implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
   implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializerVersion")

   implementation("io.ktor:ktor-server-netty:$ktorVersion")
   implementation("io.ktor:ktor-client-apache:$ktorVersion")
   implementation("io.ktor:ktor-client-json-native:$ktorVersion")
   implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
   implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
   implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

   implementation("org.slf4j:slf4j-api:$slf4jVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

   testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
      exclude(group = "junit")
   }
   testImplementation("io.ktor:ktor-client-mock-native:$ktorVersion") {
      exclude(group = "junit")
   }

   testImplementation("org.awaitility:awaitility:4.0.1")
   testImplementation("org.bouncycastle:bcpkix-jdk15on:1.64")
}

java {
   sourceCompatibility = JavaVersion.VERSION_12
   targetCompatibility = JavaVersion.VERSION_12
}

tasks.withType<KotlinCompile> {
   kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Jar>("jar") {
   archiveBaseName.set("app")

   manifest {
      attributes["Main-Class"] = "no.nav.helse.brreg.ApplicationKt"
      attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
         it.name
      }
   }

   doLast {
      configurations.runtimeClasspath.get().forEach {
         val file = File("$buildDir/libs/${it.name}")
         if (!file.exists())
            it.copyTo(file)
      }
   }
}

application {
   mainClassName = "no.nav.helse.brreg.ApplicationKt"
}

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}

tasks.withType<Wrapper> {
   gradleVersion = "6.1.1"
}
