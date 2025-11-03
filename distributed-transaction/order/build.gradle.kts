plugins {
}

tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // retry
    implementation("org.springframework.retry:spring-retry")

    implementation(project(":common"))
}

