package net.naoponju.liblis.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Pointcut("execution(* net.naoponju.liblis.controller..*.*(..))")
    fun controllerMethodExecution() {}

    @Around("controllerMethodExecution()")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature.toShortString()
        logger.info("実行開始: $signature")

        val start = System.currentTimeMillis()
        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - start
            logger.info("実行完了: $signature (実行時間 : ${executionTime}ms)")

            return result
        } catch (e: Throwable) {
            val executionTime = System.currentTimeMillis() - start
            logger.error("実行時例外が発生: $signature (実行時間: ${executionTime}ms)", e)
            throw e
        }
    }
}