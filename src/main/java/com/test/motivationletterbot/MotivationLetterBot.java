package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.AbilityService;
import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.kafka.KafkaProducer;
import com.test.motivationletterbot.kafka.KafkaRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.test.motivationletterbot.MessageConstants.*;
import static com.test.motivationletterbot.entity.BotCommandEnum.*;

import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;

@Slf4j
@Component
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final KafkaProducer kafkaProducer;
    private final AbilityService abilityService;
    private final long creatorId;

    public MotivationLetterBot(
            BotProperties botProperties,
            KafkaProducer kafkaProducer,
            TelegramClient telegramClient,
            AbilityService abilityService,
            BareboneToggle toggle) {
        super(
                telegramClient,
                botProperties.getName(),
                useInMemoryMapDB(),
                toggle
        );
        this.botProperties = botProperties;
        this.kafkaProducer = kafkaProducer;
        this.creatorId = botProperties.getBotCreatorId();
        this.abilityService = abilityService;
    }

    private static DBContext useInMemoryMapDB() {
        String useInMemory = System.getenv("USE_INMEMORY_MAPDB");
        if ("true".equalsIgnoreCase(useInMemory)) {
            // Use a unique in-memory DB name for each run, stored in a temp folder
            String dbName = "./tmpdb/inmem-" + UUID.randomUUID();
            return MapDBContext.offlineInstance(dbName);
        } else {
            return MapDBContext.onlineInstance("./tmpdb" + "/MotivationLetterBot");
        }
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    private SendMessage buildSendMessage(long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }

    public void sendMessage(long chatId, String messageText) {
        SendMessage message = buildSendMessage(chatId, messageText);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void consume(Update update) {
        var message = update.getMessage();
        if (message != null) {

            super.consume(update);

            log.warn("Current vacancy text: {}", message.getText());
            log.warn("Current motivation text: {}", message.getText());
        }
    }

    public Ability startMessageWriting() {
        return abilityService.getAbility(START);
    }

    public Ability startMotivationWriting() {
        return abilityService.getAbility(START_MOTIVATION);
    }

    public Ability endMotivationWriting() {
        return abilityService.getAbility(END_MOTIVATION);
    }

    public Ability startRoleDescriptionWriting() {
        return abilityService.getAbility(START_ROLE_DESCRIPTION);
    }

    public Ability endRoleDescriptionWriting() {
        return abilityService.getAbility(END_ROLE_DESCRIPTION);
    }

    void sendToKafka(long chatId, String messageText) {
        CompletableFuture<SendResult<String, KafkaRequest>> future = kafkaProducer.sendRequest(
                new KafkaRequest(chatId, messageText)
        );
        future.whenComplete((result, e) -> {
            if (e != null) {
                log.error("Failed to send Kafka request", e);
                sendMessage(chatId, ERROR_MESSAGE);
            }
        });
    }

    @PostConstruct
    public void init() {
        this.onRegister();
        abilityService.setBotCommands(START);
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("Registered bot '{}' (token: {}) running state is: {}", botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }
}