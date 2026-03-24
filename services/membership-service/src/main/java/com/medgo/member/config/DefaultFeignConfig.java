package com.medgo.member.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Default Feign configuration for clients that don't specify a configuration
 * This provides the default Retryer and Logger.Level beans marked as @Primary
 * to resolve ambiguity when multiple Feign configurations exist
 */
@Configuration
public class DefaultFeignConfig {

    /**
     * Default Retryer for Feign clients that don't specify a configuration
     * Marked as @Primary to resolve ambiguity when multiple Retryer beans exist
     */
    @Bean
    @Primary
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(5), 6);
    }

    /**
     * Default Logger.Level for Feign clients that don't specify a configuration
     * Marked as @Primary to resolve ambiguity when multiple Logger.Level beans exist
     */
    @Bean
    @Primary
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}



