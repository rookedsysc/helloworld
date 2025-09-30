package com.rookedsysc.common.lock

/**
 * 락 획득 실패 예외
 * Single Responsibility: 락 획득 실패 상황만 표현
 */
class LockAcquisitionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
