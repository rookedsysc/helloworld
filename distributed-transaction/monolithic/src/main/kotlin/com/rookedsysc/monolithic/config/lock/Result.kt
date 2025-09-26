package com.rookedsysc.monolithic.config.lock

/**
 * Railway Oriented Programming 패턴 구현
 * Single Responsibility: 연산 결과와 에러를 타입 안전하게 표현
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()

    /**
     * Functor - map 연산
     * Success인 경우에만 변환 함수 적용
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Monad - flatMap/bind 연산
     * 체이닝 가능한 연산 조합
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }


    companion object {
        /**
         * Try-catch를 Result로 변환하는 헬퍼
         */
        inline fun <T> catching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }
}
