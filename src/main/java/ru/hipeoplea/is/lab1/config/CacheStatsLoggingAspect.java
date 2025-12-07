package ru.hipeoplea.is.lab1.config;

import javax.cache.CacheManager;
import javax.cache.management.CacheStatisticsMXBean;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import java.lang.management.ManagementFactory;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheStatsLoggingAspect {
    private static final Logger log =
            LoggerFactory.getLogger(CacheStatsLoggingAspect.class);
    private final CacheManager cacheManager;
    private final CacheStatsProperties props;
    private final MBeanServer mBeanServer =
            ManagementFactory.getPlatformMBeanServer();

    @Around("@annotation(cacheable)")
    public Object aroundCacheable(ProceedingJoinPoint pjp,
            Cacheable cacheable) throws Throwable {
        Object res = pjp.proceed();
        logStats();
        return res;
    }

    @Around("@annotation(cachePut)")
    public Object aroundCachePut(ProceedingJoinPoint pjp,
            CachePut cachePut) throws Throwable {
        Object res = pjp.proceed();
        logStats();
        return res;
    }

    @Around("@annotation(cacheEvict)")
    public Object aroundCacheEvict(ProceedingJoinPoint pjp,
            CacheEvict cacheEvict) throws Throwable {
        Object res = pjp.proceed();
        logStats();
        return res;
    }

    private void logStats() {
        if (!props.isLoggingEnabled()) {
            return;
        }
        cacheManager.getCacheNames().forEach(name -> {
            try {
                ObjectName on = new ObjectName(String.format(
                        "javax.cache:type=CacheStatistics,"
                               + "CacheManager=%s,Cache=%s",
                        ObjectName.quote(cacheManager.getURI().toString()),
                        ObjectName.quote(name)));
                if (!mBeanServer.isRegistered(on)) {
                    return;
                }
                CacheStatisticsMXBean stats = JMX.newMXBeanProxy(
                        mBeanServer, on, CacheStatisticsMXBean.class);
                log.info(
                        "Cache [{}]: hits={}, misses={}, puts={}, evictions={}",
                        name, stats.getCacheHits(), stats.getCacheMisses(),
                        stats.getCachePuts(), stats.getCacheEvictions());
            } catch (Exception ex) {
                log.debug("Skip stats for cache {}: {}", name,
                        ex.getMessage());
            }
        });
    }
}
