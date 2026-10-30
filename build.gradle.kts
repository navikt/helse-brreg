val junitJupiterVersion = "6.1.3"
val ktorVersion = "3.6.0"
val micrometerVersion = "1.17.2"
val slf4jVersion = "2.0.20"
val logbackVersion = "1.6.5"
val logstashEncoderVersion = "9.0"
val serializerVersion = "1.12.0"
val jacksonVersion = "2.22"

group = "no.nav.helse"

plugins {
   val kotlinVersion = "2.4.21"
   kotlin("jvm") version kotlinVersion
   kotlin("plugin.serialization") version kotlinVersion
   application
}

repositories {
   mavenCentral()
   maven("https://packages.confluent.io/maven/")
}

val nettyHandlerOverriddenVersion = "4.2.19.Final"

dependencies {

   implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
   implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

   implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
   implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializerVersion")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializerVersion")

   implementation(platform("io.netty:netty-bom:$nettyHandlerOverriddenVersion")).also {
      if (ktorVersion != "3.4.3") throw RuntimeException("Slett nettyHandlerOverriddenVersion siden KTOR oppgradert?")
   }

   implementation(platform("io.ktor:ktor-bom:$ktorVersion"))
   implementation("io.ktor:ktor-server-netty")
   implementation("io.ktor:ktor-client-apache")
   implementation("io.ktor:ktor-client-json")
   implementation("io.ktor:ktor-serialization-kotlinx-json")
   implementation("io.ktor:ktor-client-content-negotiation")
   implementation("io.ktor:ktor-server-metrics-micrometer")
   implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

   implementation("org.slf4j:slf4j-api:$slf4jVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

   testImplementation("org.jetbrains.kotlin:kotlin-test:2.4.21")
   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
   testRuntimeOnly("org.junit.platform:junit-platform-launcher")

   testImplementation("io.ktor:ktor-server-test-host") {
      exclude(group = "junit")
   }
   testImplementation("io.ktor:ktor-client-mock-jvm") {
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
