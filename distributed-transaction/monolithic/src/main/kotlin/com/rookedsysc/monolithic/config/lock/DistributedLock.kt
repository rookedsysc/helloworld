package com.rookedsysc.monolithic.config.lock

import java.util.concurrent.TimeUnit

/**
 * 간단한 문자열 템플릿 기반 분산락 어노테이션
 * Single Responsibility: 락 키 템플릿과 타임아웃 설정만 담당
 *
 * @param key 락 키 템플릿 (예: "lock:order:{orderId}")
 *           {paramName} 형식으로 메서드 파라미터를 참조
 * @param waitTime 락 획득 대기 시간
 * @param leaseTime 락 보유 시간 (-1이면 자동 해제 안함)
 * @param timeUnit 시간 단위
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val waitTime: Long = 5,
    val leaseTime: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
)
