package com.roky.kafkaaz.common.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(exception: WebExchangeBindException): ResponseEntity<ApiErrorResponse> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "Invalid request"
        return error(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(exception: ResponseStatusException): ResponseEntity<ApiErrorResponse> {
        return error(exception.statusCode, exception.reason ?: exception.statusCode.toString())
    }

    private fun error(status: org.springframework.http.HttpStatusCode, message: String): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(status)
            .body(ApiErrorResponse(status.value(), message))
    }
}
