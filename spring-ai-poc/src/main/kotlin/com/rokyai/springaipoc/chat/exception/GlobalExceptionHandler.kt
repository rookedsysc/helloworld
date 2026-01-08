package com.rokyai.springaipoc.chat.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange

/**
 * 전역 예외 처리 핸들러
 *
 * 애플리케이션 전체에서 발생하는 예외를 처리하고 일관된 형식의 에러 응답을 반환합니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * WebExchangeBindException 처리
     *
     * Bean Validation (@Valid) 검증 실패 시 발생하는 예외를 처리합니다.
     * 검증 오류 메시지를 수집하여 사용자에게 반환합니다.
     *
     * @param ex 발생한 예외
     * @param exchange 요청 정보를 포함한 ServerWebExchange
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val errorMessages = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = errorMessages.ifEmpty { "유효하지 않은 요청입니다." },
            path = exchange.request.path.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * IllegalArgumentException 처리
     *
     * 잘못된 파라미터나 유효하지 않은 요청 시 발생하는 예외를 처리합니다.
     *
     * @param ex 발생한 예외
     * @param exchange 요청 정보를 포함한 ServerWebExchange
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "잘못된 요청입니다.",
            path = exchange.request.path.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * IllegalStateException 처리
     *
     * 내부 상태가 잘못되었거나 예상치 못한 상황이 발생했을 때의 예외를 처리합니다.
     *
     * @param ex 발생한 예외
     * @param exchange 요청 정보를 포함한 ServerWebExchange
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = ex.message ?: "서버 내부 오류가 발생했습니다.",
            path = exchange.request.path.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * 그 외 모든 예외 처리
     *
     * 위에서 처리되지 않은 모든 예외를 처리합니다.
     *
     * @param ex 발생한 예외
     * @param exchange 요청 정보를 포함한 ServerWebExchange
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "예상치 못한 오류가 발생했습니다: ${ex.message}",
            path = exchange.request.path.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
