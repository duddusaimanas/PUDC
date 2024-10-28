package com.user.management.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

@Aspect
@Component
public class LoggingAspect {

    private static final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
    private int loggerLevel = 0;

    @Pointcut("execution(public * com.user.management.*.*.*(..))")
    private void publicMethodsFromLoggingPackage() {
    }

    @AfterThrowing(pointcut = "publicMethodsFromLoggingPackage()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String target = joinPoint.getTarget().toString();
        int levels = target.split("\\.").length - 1;
        String targetClass = target.split("@")[0];
        String className = targetClass.split("\\.")[levels];
        String methodName = joinPoint.getSignature().getName();
        loggerFactory.getLogger(targetClass).error("|!<{} {}.{}() - {}|",
                loggerLevel > 0 ? "-".repeat(loggerLevel--) : "",
                className, methodName, exception.getMessage(), exception);
    }

    @Before(value = "publicMethodsFromLoggingPackage()")
    public void logBefore(JoinPoint joinPoint) throws JsonProcessingException {
        Object[] args = joinPoint.getArgs();
        String[] argNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        hideSensitive(args, argNames);
        String target = joinPoint.getTarget().toString();
        int levels = target.split("\\.").length - 1;
        String targetClass = target.split("@")[0];
        String className = targetClass.split("\\.")[levels];
        String methodName = joinPoint.getSignature().getName();
        loggerFactory.getLogger(targetClass).info("|{}> {}.{}() - {}|",
                loggerLevel >= 0 ? "-".repeat(++loggerLevel) : "",
                className, methodName, args);
    }

    @AfterReturning(value = "publicMethodsFromLoggingPackage()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String target = joinPoint.getTarget().toString();
        int levels = target.split("\\.").length - 1;
        String targetClass = target.split("@")[0];
        String className = targetClass.split("\\.")[levels];
        String methodName = joinPoint.getSignature().getName();
        loggerFactory.getLogger(targetClass).info("|<{} {}.{}() - {}|",
                loggerLevel > 0 ? "-".repeat(loggerLevel--) : "",
                className, methodName, result);
    }

    private void hideSensitive(Object[] args, String[] argNames) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                String parameterName = argNames[i].toLowerCase();
                if (parameterName.contains("password") || parameterName.contains("secret")) {
                    args[i] = "*".repeat(8);
                }
            }
        }
    }
}
