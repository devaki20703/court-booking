package com.courtbooking.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Value("${app.performance.threshold:500}")
    private long thresholdMs;

    @Around("within(com.courtbooking.*.controller..*) || within(com.courtbooking.*.service..*)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        long startTime = System.nanoTime();
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            if (durationMs > thresholdMs) {
                logger.warn("[PERFORMANCE-SLOW] {}.{}() | Duration: {}ms | Threshold: {}ms | SLOW API",
                    className, methodName, durationMs, thresholdMs);
            } else {
                logger.debug("[PERFORMANCE] {}.{}() | Duration: {}ms",
                    className, methodName, durationMs);
            }
        }
    }

    public void setThresholdMs(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }
}