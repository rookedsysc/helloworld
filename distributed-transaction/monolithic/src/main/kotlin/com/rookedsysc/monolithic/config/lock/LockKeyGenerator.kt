package com.rookedsysc.monolithic.config.lock

import org.aspectj.lang.ProceedingJoinPoint

/**
 * 락 키 생성 전략 인터페이스
 * Strategy Pattern: 다양한 락 키 생성 전략을 추상화
 * Interface Segregation: 단일 책임의 작은 인터페이스
 */
interface LockKeyGenerator {
    /**
     * 주어진 템플릿과 메서드 정보를 기반으로 락 키 생성
     *
     * @param template 락 키 템플릿
     * @param joinPoint AOP JoinPoint (메서드 정보 포함)
     * @return 생성된 락 키
     */
    fun generate(template: String, joinPoint: ProceedingJoinPoint): String
}