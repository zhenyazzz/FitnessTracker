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
public class ControllerLoggingAspect {

    private static final String CONTROLLER_DIVIDER = "=".repeat(70);
    private static final String ARG_INDENT = "  ";

    @Pointcut("execution(* org.example.fitnesstracker.controller.*.*(..))")
    public void controllerLayers() {}

    @Around("controllerLayers()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String method = sig.getMethod().getName();
        String fullMethodName = className + "." + method;
        String args = formatArgs(joinPoint.getArgs());

        long started = System.currentTimeMillis();

        log.info("""
                {}
                Controller: {}
                Arguments:
                {}
                ----------------------------------------
                """,
                CONTROLLER_DIVIDER, fullMethodName, args);

        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - started;

        log.info("""
                Response:
                {}
                Execution time: {} ms
                {}
                """,
                formatResult(result),
                duration,
                CONTROLLER_DIVIDER);

        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return ARG_INDENT + "без параметров";
        }
        return Arrays.stream(args)
            .map(arg -> ARG_INDENT + (arg == null ? "null" : arg.toString()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatResult(Object result) {
        if (result instanceof ResponseEntity<?> response) {
            String bodyStr = response.getBody() != null ? response.getBody().toString() : "null";
            return ARG_INDENT + "status=" + response.getStatusCode().value() + System.lineSeparator() +
                   ARG_INDENT + "body=" + bodyStr;
        }
        return ARG_INDENT + String.valueOf(result);
    }
}
