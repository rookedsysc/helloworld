package com.rokyai.springaipoc.helloworld.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {
    // WebFlux + Coroutines 환경에서는 suspend 함수를 사용하는 것이 정석입니다.
    // 내부적으로 Non-blocking으로 동작합니다.
    @GetMapping("/hello")
    suspend fun hello(): String {
        return "Hello World! WebFlux with Coroutines 🚀"
    }
}
