package org.example.fitnesstracker.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {

    @Pointcut("within(org.example.fitnesstracker..*) && !within(org.example.fitnesstracker.security..*)")
    public void applicationPackages() {}

    @AfterThrowing(pointcut = "applicationPackages()", throwing = "exception")
    public void logCustomExceptions(JoinPoint joinPoint, Throwable exception) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();

        log.error("----- Exception detected -----");
        log.error("Source: {}.{}()", className, methodName);
        log.error("Exception: {}", exception.getClass().getSimpleName());
        log.error("Message: {}", exception.getMessage());
        log.error("------------------------------");
        log.debug("Stacktrace: ", exception);
    }
}

