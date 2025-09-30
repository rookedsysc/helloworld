plugins {
}

tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    runtimeOnly("com.mysql:mysql-connector-j")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // redisson : https://mvnrepository.com/artifact/org.redisson/redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.51.0")

    // prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}

