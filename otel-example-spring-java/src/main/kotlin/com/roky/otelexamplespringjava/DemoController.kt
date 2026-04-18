package com.roky.otelexamplespringjava

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/demo")
class DemoController(
    private val demoService: DemoService,
) {
    @GetMapping("/success")
    fun success(): Map<String, String> = demoService.success()

    @GetMapping("/error")
    fun error(): Map<String, String> = demoService.error()
}
