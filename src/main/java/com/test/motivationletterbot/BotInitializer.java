package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotInitializer implements InitializingBean {
    private final BotProperties botProperties;

    public BotInitializer(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    @Override
    public void afterPropertiesSet() {
        if (botProperties.getToken() == null || botProperties.getToken().isBlank()) {
            log.error("Bot '{}' token must not be null or blank", botProperties.getName());
            throw new IllegalStateException("Bot token is null or blank");
        }
    }
}
