package org.example.fitnesstracker.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    private static final String INDENT = "    ";

    @Pointcut("within(org.example.fitnesstracker.service.WorkoutsService) || " +
              "within(org.example.fitnesstracker.service.MediaService) || " +
              "within(org.example.fitnesstracker.service.AuthService) || " +
              "within(org.example.fitnesstracker.service.AnalyticsService)")
    public void monitoredServices() {}

    @Around("monitoredServices()")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();
        String args = formatArgs(joinPoint.getArgs());

        log.info("""
                --> Service call {}.{}()
                {}""",
                className, methodName, args);

        Object result = joinPoint.proceed();

        log.info("""
                <-- Service result {}.{}()
                {}
                """,
                className, methodName, formatResult(result));
        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return INDENT + "без параметров";
        }
        return INDENT + "Arguments:\n" + Arrays.stream(args)
            .map(arg -> INDENT + INDENT + (arg == null ? "null" : arg.toString()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatResult(Object result) {
        if (result instanceof ResponseEntity<?> response) {
            Object body = response.getBody();
            return INDENT + "status=" + response.getStatusCode().value() + System.lineSeparator()
                    + INDENT + "body=" + (body != null ? body : "null");
        }
        return INDENT + String.valueOf(result);
    }
}
