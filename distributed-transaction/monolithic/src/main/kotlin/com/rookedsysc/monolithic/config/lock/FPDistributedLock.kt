package com.rookedsysc.monolithic.config.lock

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

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
     * 락 내에서 작업 실행
     * Open/Closed: 실행 로직은 외부에서 주입받아 확장 가능
     */
    fun <T> withLock(config: DistributedLockConfig, block: () -> T): T {
        val lock = if (config.fairLock) {
            redissonClient.getFairLock(config.key)
        } else {
            redissonClient.getLock(config.key)
        }

        logger.debug("분산락 획득 시도 - key: ${config.key}")

        val acquired = try {
            lock.tryLock(
                config.waitTime,
                config.leaseTime,
                config.timeUnit
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw LockAcquisitionException("락 획득 중 인터럽트 발생 - key: ${config.key}", e)
        }

        if (!acquired) {
            throw LockAcquisitionException(
                "락 획득 실패 - key: ${config.key} " +
                "(대기 시간: ${config.waitTime}${config.timeUnit})"
            )
        }

        logger.debug("분산락 획득 성공 - key: ${config.key}")

        return try {
            block()
        } finally {
            try {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                    logger.debug("분산락 해제 - key: ${config.key}")
                }
            } catch (e: Exception) {
                logger.error("분산락 해제 실패 - key: ${config.key}", e)
            }
        }
    }
}
