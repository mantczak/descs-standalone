package edu.put.ma.descs.algorithms.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ProcessingTimeLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingTimeLogger.class);

    @Pointcut("@annotation(ProcessingTimeLog)")
    public void annotationPointCutDefinition() {
        // ProcessingTimeLog annotation point cut definition
    }

    @Pointcut("execution(* extendAlignment(..))")
    public void atAlignmentFindingExecution() {
        // extend alignment execution definition
    }

    @Around("@annotation(ProcessingTimeLog) && execution(* extendAlignment(..))")
    public Object aroundAlignmentFindingAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        return logProcessingTime(joinPoint, "Alignment finding");
    }

    @Pointcut("execution(* build(..))")
    public void atDescriptorsBuildingExecution() {
        // build execution definition
    }

    @Around("@annotation(ProcessingTimeLog) && execution(* build(..))")
    public Object aroundDescriptorsBuildingAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        return logProcessingTime(joinPoint, "Descriptors building");
    }

    private static final Object logProcessingTime(final ProceedingJoinPoint joinPoint,
            final String description) throws Throwable {
        final long startMillis = System.currentTimeMillis();
        Object returnObject = null;
        try {
            returnObject = joinPoint.proceed();
        } finally {
            final long duration = System.currentTimeMillis() - startMillis;
            LOGGER.info(String.format("%s procedure took %d [ms]", description, duration));
        }
        return returnObject;
    }
}
