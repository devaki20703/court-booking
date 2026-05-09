package com.courtbooking.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.courtbooking.*.controller..*) || within(com.courtbooking.*.service..*)")
    public void controllerAndServicePointcut() {}

    @Before("controllerAndServicePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        logger.info("[ENTER] {}.{}() | Args: {}",
            className, methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "controllerAndServicePointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.info("[EXIT] {}.{}() | Result: {}",
            className, methodName, result != null ? result.getClass().getSimpleName() : "void");
    }

    @AfterThrowing(pointcut = "controllerAndServicePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.error("[EXCEPTION] {}.{}() | Exception: {} | Message: {}",
            className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }

    @Around("controllerAndServicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("[TIMING] {}.{}() | Execution Time: {}ms",
                className, methodName, executionTime);

            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[TIMING-ERROR] {}.{}() | Failed after: {}ms | Error: {}",
                className, methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }
}