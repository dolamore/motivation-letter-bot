package com.test.motivationletterbot.config;

import com.openai.errors.UnauthorizedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // No retry for non-transient configuration/auth errors
        FixedBackOff fixedBackOff = new FixedBackOff(0L, 0L);
        DefaultErrorHandler handler = new DefaultErrorHandler(fixedBackOff);
        handler.addNotRetryableExceptions(UnauthorizedException.class);
        return handler;
    }
}

