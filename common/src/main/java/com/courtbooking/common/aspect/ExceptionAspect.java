package com.courtbooking.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ExceptionAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAspect.class);

    private static final Map<String, String> EXCEPTION_HANDLERS = new HashMap<>();

    static {
        EXCEPTION_HANDLERS.put("BadRequestException", "INVALID_REQUEST");
        EXCEPTION_HANDLERS.put("NotFoundException", "RESOURCE_NOT_FOUND");
        EXCEPTION_HANDLERS.put("UnauthorizedException", "UNAUTHORIZED");
        EXCEPTION_HANDLERS.put("ForbiddenException", "FORBIDDEN");
        EXCEPTION_HANDLERS.put("ConflictException", "CONFLICT");
    }

    @AfterThrowing(pointcut = "within(com.courtbooking.*.service..*)", throwing = "exception")
    public void handleException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String exceptionClass = exception.getClass().getSimpleName();
        String errorCode = EXCEPTION_HANDLERS.getOrDefault(exceptionClass, "INTERNAL_ERROR");

        logger.error(
            "[EXCEPTION-HANDLED] {} | {}() | ErrorCode: {} | Exception: {} | Message: {} | StackTrace: {}",
            className,
            methodName,
            errorCode,
            exceptionClass,
            exception.getMessage(),
            getFirstFewStackTrace(exception)
        );
    }

    @AfterThrowing(pointcut = "within(com.courtbooking.*.controller..*)", throwing = "exception")
    public void handleControllerException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.error(
            "[CONTROLLER-EXCEPTION] {}.{}() | Unhandled Exception: {} | Message: {}",
            className,
            methodName,
            exception.getClass().getSimpleName(),
            exception.getMessage()
        );
    }

    private String getFirstFewStackTrace(Throwable exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length == 0) {
            return "No stack trace available";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("com.courtbooking")) {
                if (count > 0) sb.append(" -> ");
                sb.append(element.getClassName()).append(":").append(element.getLineNumber());
                count++;
                if (count >= 3) break;
            }
        }
        return sb.toString();
    }
}