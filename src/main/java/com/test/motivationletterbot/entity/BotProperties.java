package com.test.motivationletterbot.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
@Data
public class BotProperties {
    private String name;
    private String token;
    private long botCreatorId;
}
