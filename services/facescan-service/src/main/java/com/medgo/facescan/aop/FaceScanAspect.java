package com.medgo.facescan.aop;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Slf4j
@Component
public class FaceScanAspect {

    @PostConstruct
    public void init() {
        log.debug("FaceScanAspect is loaded and active");
    }

    @Before("execution(* com.medicard.facescan.service.impl.FaceScanServiceImpl.*(..))")
    public void beforeAdvice(JoinPoint joinPoint){
        log.debug("Request to " + joinPoint.getSignature() + " started at " + new Date() );
    }

    @After("execution(* com.medicard.facescan.service.impl.FaceScanServiceImpl.*(..))")
    public void aftereAdvice(JoinPoint joinPoint){
        log.debug("Request to " + joinPoint.getSignature() + " ended at " + new Date() );
    }

}