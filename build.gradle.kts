val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val prometeusVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val kodeinVersion: String by project
val jbcryptVersion: String by project

plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

group = "com"
version = "0.0.1"

application {
    mainClass.set("siberia.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    fun ktor(part: String, module: String) = "io.ktor:ktor-$part-$module-jvm"
    fun ktorServer(module: String) = ktor(part = "server", module = module)
    fun ktorClient(module: String) = ktor(part = "client", module = module)
    fun jetBrains(module: String, version: String) = "org.jetbrains.$module:$version"
    fun kotlin(module: String) = jetBrains("kotlin:kotlin-$module", kotlinVersion)

    //Ktor server
    implementation(ktorServer("core"))
    implementation(ktorServer("auth"))
    implementation(ktorServer("auth-jwt"))
    implementation(ktorServer("host-common"))
    implementation(ktorServer("status-pages"))
    implementation(ktorServer("call-logging"))
    implementation(ktorServer("call-id"))
    implementation(ktorServer("metrics-micrometer"))
    implementation(ktorServer("content-negotiation"))
    implementation(ktor("serialization", "kotlinx-json"))
    implementation(ktorServer("websockets"))
    implementation(ktorServer("netty"))

    //Crypto
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")

    //DI
    implementation("org.kodein.di:kodein-di-jvm:$kodeinVersion")

    //Database
    api(jetBrains("exposed:exposed-core", exposedVersion))
    api(jetBrains("exposed:exposed-dao", exposedVersion))
    api(jetBrains("exposed:exposed-jdbc", exposedVersion))
    api(jetBrains("exposed:exposed-java-time", exposedVersion))
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
//    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")
//    implementation("mysql:mysql-connector-java:8.0.31")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.6")

    //Kotlinx coroutines
    runtimeOnly(kotlin("reflect", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation(jetBrains("kotlinx:kotlinx-coroutines-core", "1.7.0-RC"))

    //Logging and metrics
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeusVersion")

    //Tests
    testImplementation(ktorServer("tests"))
    testImplementation(kotlin("test-junit"))
}
