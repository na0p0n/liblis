package net.naoponju.liblis.config

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Aspect
@Component
class LoggingAspect {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Before("execution(* net.naoponju.liblis.controller..*.*(..))")
    fun logBefore(joinPoint: JoinPoint) {
        logger.info("実行開始: ${joinPoint.signature.toShortString()}")
    }

    @AfterReturning("execution(* net.naoponju.liblis.controller..*.*(..))")
    fun logAfter(joinPoint: JoinPoint) {
        logger.info("実行完了: ${joinPoint.signature.toShortString()}")
    }
}