package ru.hipeoplea.is.lab1.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "cache.stats")
public class CacheStatsProperties {
    private boolean loggingEnabled = false;
}
