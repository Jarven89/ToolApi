package com.xtyu.toolapi.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;


@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // 定义切点，拦截所有 Controller 层的请求
    @Pointcut("execution(* com.xtyu.toolapi.controller..*(..))")
    public void logPointCut() {
    }

    // 前置通知，在请求处理之前打印请求信息
    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        logger.info("Request URL : {}", request.getRequestURL().toString());
        logger.info("Request Method : {}", request.getMethod());
        logger.info("Request IP : {}", request.getRemoteAddr());
        logger.info("Class Method : {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info("Request Args : {}", Arrays.toString(joinPoint.getArgs()));
    }

    // 后置通知，在请求处理完成并返回结果后打印返回信息
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Response : {}", result);
    }


    // 环绕通知，可同时实现前置和后置通知功能，提供更多的控制，如异常处理
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("Time Taken : {} ms", endTime - startTime);
            return result;
        } catch (Throwable e) {
            logger.error("Exception : {}", e.getMessage());
            throw e;
        }
    }
}