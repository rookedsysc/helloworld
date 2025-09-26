package com.rookedsysc.monolithic.config.lock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 함수형 프로그래밍 스타일의 분산락 구현
 * Higher-Order Functions와 Pure Functions 중심 설계
 *
 * SOLID 원칙:
 * - Single Responsibility: 분산락 연산 조합만 담당
 * - Open/Closed: 새로운 락 전략은 함수 조합으로 확장
 * - Dependency Inversion: RedissonClient 추상화에 의존
 *
 * Design Pattern: Monad Pattern, Strategy Pattern (함수형)
 */
@Component
class FPDistributedLock(
    private val redissonClient: RedissonClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)


    /**
     * 핵심 Higher-Order Function
     * 락 내에서 작업을 실행하고 Result를 반환
     *
     * @param config 락 설정
     * @param block 실행할 작업 (suspend 가능)
     * @return Result<T> 작업 결과 또는 에러
     */
    fun <T> withLock(config: DistributedLockConfig, block: () -> T): Result<T> {
        val lock = if (config.fairLock) {
            redissonClient.getFairLock(config.key)
        } else {
            redissonClient.getLock(config.key)
        }

        logger.debug("분산락 획득 시도 - key: ${config.key}")

        return acquireLock(lock, config)
            .flatMap { executeLocked(lock, config.key, block) }
    }


    // Private helper functions

    private fun acquireLock(lock: RLock, config: DistributedLockConfig): Result<Unit> {
        return try {
            val acquired = lock.tryLock(
                config.waitTime,
                config.leaseTime,
                config.timeUnit
            )
            if (acquired) {
                logger.debug("분산락 획득 성공 - key: ${config.key}")
                Result.Success(Unit)
            } else {
                Result.Failure(
                    LockAcquisitionException(
                        "락 획득 실패 - key: ${config.key} " +
                        "(대기 시간: ${config.waitTime}${config.timeUnit})"
                    )
                )
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Result.Failure(
                LockAcquisitionException("락 획득 중 인터럽트 발생 - key: ${config.key}", e)
            )
        }
    }

    private fun <T> executeLocked(
        lock: RLock,
        lockKey: String,
        block: () -> T
    ): Result<T> {
        return try {
            Result.catching(block)
        } finally {
            releaseLock(lock, lockKey)
        }
    }

    private fun releaseLock(lock: RLock, lockKey: String) {
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

