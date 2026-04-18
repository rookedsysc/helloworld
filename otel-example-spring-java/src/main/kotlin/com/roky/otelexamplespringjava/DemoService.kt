package com.roky.otelexamplespringjava

import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DemoService(
    meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val successCounter = meterRegistry.counter("demo_requests_total", "result", "success")
    private val errorCounter = meterRegistry.counter("demo_requests_total", "result", "error")

    @WithSpan("demo.success")
    fun success(): Map<String, String> {
        log.info("demo success request received")
        Span.current().setAttribute("app.demo.result", "success")
        successCounter.increment()

        return mapOf("message" to "ok")
    }

    @WithSpan("demo.error")
    fun error(): Map<String, String> {
        log.error("demo error request received")
        Span.current().setAttribute("app.demo.result", "error")
        errorCounter.increment()
        throw IllegalStateException("demo error")
    }
}
