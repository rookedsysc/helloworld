package com.rookedsysc.monolithic.config.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Locale
import java.util.Locale.getDefault

/**
 * 문자열 템플릿 기반 락 키 생성기
 * Single Responsibility: 템플릿 문자열을 실제 값으로 치환하는 책임만 담당
 * Open/Closed: 새로운 템플릿 패턴 추가는 가능하나 기존 로직 수정 불필요
 */
@Component
class StringTemplateLockKeyGenerator : LockKeyGenerator {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 템플릿의 {paramName} 형식을 메서드 파라미터 값으로 치환
     * Reflection 대신 직접 파라미터 매핑을 통해 성능 최적화
     *
     * @param template "lock:order:{orderId}" 같은 템플릿
     * @param joinPoint 메서드 실행 정보
     * @return "lock:order:123" 같은 실제 락 키
     */
    override fun generate(template: String, joinPoint: ProceedingJoinPoint): String {
        val signature = joinPoint.signature as MethodSignature
        // ex: placeOrderCommand
        val parameterNames = signature.parameterNames ?: return template
        val args = joinPoint.args

        // 파라미터 이름과 값을 매핑
        val parameterMap = mutableMapOf<String, Any?>()
        parameterNames.forEachIndexed { index, name ->
            parameterMap[name] = args.getOrNull(index)
        }

        // 템플릿의 {paramName} 부분을 실제 값으로 치환
        var result = template
        val pattern = "\\{([^}]+)\\}".toRegex()

        pattern.findAll(template).forEach { matchResult ->
            val paramName = matchResult.groupValues[1]
            val value = extractValue(paramName, parameterMap)

            if (value != null) {
                result = result.replace("{$paramName}", value.toString())
            } else {
                logger.warn("파라미터 '$paramName'를 찾을 수 없습니다. 템플릿: $template")
            }
        }

        logger.debug("생성된 락 키: $result (템플릿: $template)")
        return result
    }

    /**
     * 중첩된 속성 접근 지원 (예: "order.id")
     * Single Responsibility: 값 추출 로직만 담당
     */
    private fun extractValue(path: String, parameterMap: Map<String, Any?>): Any? {
        val parts = path.split(".")

        return if (parts.size == 1) {
            // 단순 파라미터
            parameterMap[path]
        } else {
            // 중첩 속성 (예: order.id)
            val rootParam: Any? = parameterMap[parts[0]]
            if (rootParam == null) {
                logger.warn("루트 파라미터 '${parts[0]}'를 찾을 수 없습니다.")
                return null
            }
            extractNestedValue(rootParam, parts.drop(1))
        }
    }

    /**
     * 객체의 중첩 속성 값 추출
     * 성능을 위해 캐시 가능한 메타데이터 사용
     * ex: path = ["order", "id"]
     */
    private fun extractNestedValue(obj: Any, path: List<String>): Any? {
        var current: Any? = obj

        for (property in path) {
            if (current == null) return null

            current = try {
                // Kotlin property 접근
                val field = current::class.java.getDeclaredField(property)
                field.isAccessible = true
                field.get(current)
            } catch (e: NoSuchFieldException) {
                try {
                    // Getter 메서드 시도 (get + 대문자)
                    val getterName =
                        "get${property.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }}"
                    val method = current::class.java.getMethod(getterName)
                    method.invoke(current)
                } catch (e: Exception) {
                    logger.debug("속성 '$property' 추출 실패: ${e.message}")
                    null
                }
            }
        }

        return current
    }
}
