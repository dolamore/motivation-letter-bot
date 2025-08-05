package com.test.motivationletterbot.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String name;
    private String token;
    private Long botCreatorId;
    private boolean useInmemoryDb;
}
