package com.courtbooking.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
public class TraceAspect {

    private static final Logger logger = LoggerFactory.getLogger(TraceAspect.class);
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Around("within(com.courtbooking.*.controller..*)")
    public Object traceRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();
        String traceId = extractOrGenerateTraceId(request);

        MDC.put(TRACE_ID_MDC_KEY, traceId);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.info("[TRACE] TraceId: {} | {} -> {}.{}() | URI: {} | Method: {}",
            traceId,
            getClientIp(request),
            className,
            methodName,
            request != null ? request.getRequestURI() : "N/A",
            request != null ? request.getMethod() : "N/A"
        );

        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String status = exception != null ? "ERROR" : "SUCCESS";

            logger.info("[TRACE-COMPLETE] TraceId: {} | Status: {} | Duration: {}ms | {}.{}()",
                traceId, status, duration, className, methodName);

            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    public static String extractOrGenerateTraceId(HttpServletRequest request) {
        if (request != null) {
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId != null && !traceId.isEmpty()) {
                return traceId;
            }
        }
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}