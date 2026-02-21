package net.naoponju.liblis.common.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

@Aspect
@Component
class LoggingAspect {
    @Pointcut("execution(* net.naoponju.liblis.controller..*.*(..))")
    @Suppress("EmptyFunctionBlock")
    fun controllerMethodExecution() {}

    @Around("controllerMethodExecution()")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature.toShortString()
        logger.info("実行開始: $signature")

        val stopWatch = StopWatch()
        stopWatch.start()

        val result = runCatching { joinPoint.proceed() }

        if (stopWatch.isRunning) {
            stopWatch.stop()
        }

        result
            .onSuccess {
                logger.info("実行完了: $signature (実行時間 : ${stopWatch.totalTimeMillis}ms)")
            }
            .onFailure { e ->
                logger.error("実行時例外が発生: $signature (実行時間: ${stopWatch.totalTimeMillis}ms)", e)
            }

        return result.getOrThrow()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)
    }
}
