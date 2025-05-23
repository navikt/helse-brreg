val junitJupiterVersion = "5.12.2"
val ktorVersion = "3.1.2"
val micrometerVersion = "1.3.20"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.3.15"
val logstashEncoderVersion = "7.4"
val serializerVersion = "1.8.1"
val jacksonVersion = "2.15.2"

group = "no.nav.helse"

plugins {
   val kotlinVersion = "2.1.20"
   kotlin("jvm") version kotlinVersion
   kotlin("plugin.serialization") version kotlinVersion
   application
}

repositories {
   mavenCentral()
   maven("https://packages.confluent.io/maven/")
}

dependencies {

   implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

   implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
   implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializerVersion")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializerVersion")

   implementation("io.ktor:ktor-server-netty:$ktorVersion")
   implementation("io.ktor:ktor-client-apache:$ktorVersion")
   implementation("io.ktor:ktor-client-json:$ktorVersion")
   implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
   implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
   implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
   implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

   implementation("org.slf4j:slf4j-api:$slf4jVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

   testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.10")
   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
   testRuntimeOnly("org.junit.platform:junit-platform-launcher")

   testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
      exclude(group = "junit")
   }
   testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion") {
      exclude(group = "junit")
   }
}

java {
   sourceCompatibility = JavaVersion.VERSION_21
   targetCompatibility = JavaVersion.VERSION_21
}

tasks {
   kotlin {
      jvmToolchain(21)
   }
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
         val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
         if (!file.exists())
            it.copyTo(file)
      }
   }
}

application {
   mainClass.set("no.nav.helse.brreg.ApplicationKt")
}

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}
