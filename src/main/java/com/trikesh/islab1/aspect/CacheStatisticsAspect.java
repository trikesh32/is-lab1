package com.trikesh.islab1.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;

@Slf4j
@Aspect
@Component
public class CacheStatisticsAspect {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private boolean loggingEnabled = false;

    public void enableLogging() {
        loggingEnabled = true;
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        log.info("Cache statistics logging enabled");
    }

    public void disableLogging() {
        loggingEnabled = false;
        log.info("Cache statistics logging disabled");
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @Around("execution(* com.trikesh.islab1.repository.*.*(..))")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (loggingEnabled) {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics statistics = sessionFactory.getStatistics();

            log.info("=== Cache Statistics ===");
            log.info("Second Level Cache Hit Count: {}", statistics.getSecondLevelCacheHitCount());
            log.info("Second Level Cache Miss Count: {}", statistics.getSecondLevelCacheMissCount());
            log.info("Second Level Cache Put Count: {}", statistics.getSecondLevelCachePutCount());
            log.info("Query Cache Hit Count: {}", statistics.getQueryCacheHitCount());
            log.info("Query Cache Miss Count: {}", statistics.getQueryCacheMissCount());
            log.info("Query Cache Put Count: {}", statistics.getQueryCachePutCount());
            log.info("========================");
        }

        return result;
    }
}