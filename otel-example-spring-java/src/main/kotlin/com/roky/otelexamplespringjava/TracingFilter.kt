package com.roky.otelexamplespringjava

import io.opentelemetry.api.trace.Span
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class TracingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val transactionId = request.getHeader(TRANSACTION_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

        response.setHeader(TRANSACTION_ID_HEADER, transactionId)

        try {
            MDC.put("transactionId", transactionId)
            val currentSpan = Span.current()
            if (currentSpan.spanContext.isValid) {
                currentSpan.setAttribute("app.transaction_id", transactionId)
                currentSpan.setAttribute("http.request.header.x_transaction_id", transactionId)
            }
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("transactionId")
        }
    }

    private companion object {
        const val TRANSACTION_ID_HEADER = "X-Transaction-Id"
    }
}
