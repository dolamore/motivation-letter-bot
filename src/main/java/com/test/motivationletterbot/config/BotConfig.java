package com.test.motivationletterbot.config;

import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.entity.keyboard.InlineKeyboards;
import com.test.motivationletterbot.entity.UserSession;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
@EnableConfigurationProperties(BotProperties.class)
public class BotConfig {
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BotExecutor-");
        executor.setRejectedExecutionHandler((r, e) -> {
            // Use CallerRunsPolicy as fallback
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, e);
        });
        executor.initialize();
        return executor;
    }

    @Bean
    public TelegramClient telegramClient(BotProperties botProperties) {
        return new OkHttpTelegramClient(botProperties.getToken());
    }

    @Bean
    public ConcurrentHashMap<Long, UserSession> userSessions() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public SilentSender silentSender(TelegramClient telegramClient) {
        return new SilentSender(telegramClient);
    }

    @Bean
    public BareboneToggle bareboneToggle() {
        return new BareboneToggle();
    }

    @Autowired
    private InlineKeyboards inlineKeyboards;

    @PostConstruct
    public void injectInlineKeyboards() {
        UserSession.setInlineKeyboards(inlineKeyboards);
    }
}