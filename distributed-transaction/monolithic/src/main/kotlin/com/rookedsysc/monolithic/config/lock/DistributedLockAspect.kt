package com.rookedsysc.monolithic.config.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 간단한 분산락 AOP 처리 클래스
 * Single Responsibility: 템플릿 기반 분산락 처리만 담당
 * Dependency Inversion: LockKeyGenerator 추상화에 의존
 * Open/Closed: 새로운 키 생성 전략은 LockKeyGenerator 구현체로 확장
 */
@Aspect
@Component
@Order(1) // DistributedLockAspect보다 우선순위 높게 설정
class DistributedLockAspect(
    private val redissonClient: RedissonClient,
    private val lockKeyGenerator: LockKeyGenerator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * SimpleLock 어노테이션이 적용된 메서드에 대한 AOP 처리
     * Strategy Pattern: lockKeyGenerator를 통해 키 생성 전략 위임
     */
    @Around("@annotation(distributedLock)")
    fun lockAroundMethod(
        joinPoint: ProceedingJoinPoint,
        distributedLock: DistributedLock
    ): Any? {
        // 템플릿 기반 락 키 생성 (Reflection 없이 빠른 처리)
        val lockKey = lockKeyGenerator.generate(distributedLock.key, joinPoint)
        val lock = redissonClient.getFairLock(lockKey)

        logger.debug("분산락 획득 시도 - key: $lockKey (템플릿: ${distributedLock.key})")

        val acquired = try {
            lock.tryLock(
                distributedLock.waitTime,
                distributedLock.leaseTime,
                distributedLock.timeUnit
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw SimpleLockException("락 획득 중 인터럽트 발생 - key: $lockKey", e)
        }

        if (!acquired) {
            throw SimpleLockException(
                "락 획득 실패 - key: $lockKey " +
                "(대기 시간: ${distributedLock.waitTime}${distributedLock.timeUnit})"
            )
        }

        logger.debug("분산락 획득 성공 - key: $lockKey")

        return try {
            // 실제 메서드 실행
            joinPoint.proceed()
        } finally {
            // 락 해제
            releaseLock(lock, lockKey)
        }
    }

    /**
     * 락 해제 로직 분리
     * Single Responsibility: 락 해제 책임만 담당
     */
    private fun releaseLock(lock: org.redisson.api.RLock, lockKey: String) {
        try {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                logger.debug("분산락 해제 - key: $lockKey")
            }
        } catch (e: Exception) {
            logger.error("분산락 해제 실패 - key: $lockKey", e)
        }
    }
}

/**
 * SimpleLock 전용 예외 클래스
 * Single Responsibility: SimpleLock 실패 상황만 표현
 */
class SimpleLockException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
