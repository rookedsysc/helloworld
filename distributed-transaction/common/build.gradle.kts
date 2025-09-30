plugins {
}

tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    // redisson : https://mvnrepository.com/artifact/org.redisson/redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.51.0")
}

