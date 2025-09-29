package com.rookedsysc.monolithic.config.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 간단한 분산락 AOP 처리 클래스
 * Single Responsibility: 템플릿 기반 분산락 처리만 담당
 * Dependency Inversion: LockKeyGenerator 추상화에 의존
 * Open/Closed: 새로운 키 생성 전략은 LockKeyGenerator 구현체로 확장
 */
@Aspect
@Component
class DistributedLockAspect(
    private val redissonClient: RedissonClient,
    private val lockKeyGenerator: LockKeyGenerator,
    private val transactionTemplate: TransactionTemplate
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * SimpleLock 어노테이션이 적용된 메서드에 대한 AOP 처리
     * Strategy Pattern: lockKeyGenerator를 통해 키 생성 전략 위임
     */
    @Around("@annotation(distributedLockWithTransaction)")
    fun lockAroundMethod(
        joinPoint: ProceedingJoinPoint,
        distributedLockWithTransaction: DistributedLockWithTransaction
    ): Any? {
        // 템플릿 기반 락 키 생성 (Reflection 없이 빠른 처리)
        val lockKey = lockKeyGenerator.generate(distributedLockWithTransaction.key, joinPoint)
        val lock = if (distributedLockWithTransaction.fairLock) {
            redissonClient.getFairLock(lockKey)
        } else {
            redissonClient.getLock(lockKey)
        }

        logger.debug("분산락 획득 시도 - key: $lockKey (템플릿: ${distributedLockWithTransaction.key})")

        val acquired = try {
            lock.tryLock(
                distributedLockWithTransaction.waitTime,
                distributedLockWithTransaction.leaseTime,
                distributedLockWithTransaction.timeUnit
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw SimpleLockException("락 획득 중 인터럽트 발생 - key: $lockKey", e)
        }

        if (!acquired) {
            throw SimpleLockException(
                "락 획득 실패 - key: $lockKey " +
                        "(대기 시간: ${distributedLockWithTransaction.waitTime}${distributedLockWithTransaction.timeUnit})"
            )
        }

        logger.debug("분산락 획득 성공 - key: $lockKey")

        return try {
            transactionTemplate.execute { status ->
                try {
                    joinPoint.proceed()
                } catch (e: Exception) {
                    status.setRollbackOnly()
                    logger.error("트랜잭션 롤백 - key: $lockKey", e)
                    throw e
                }
            }
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
            lock.unlock()
            logger.debug("분산락 해제 - key: $lockKey")
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
