plugins {
    kotlin("jvm") version "1.9.25"
}

group = "com.rookedsysc"
version = "0.0.1-SNAPSHOT"
description = "distributed-transaction"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }
}
