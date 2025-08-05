package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.*;
import com.test.motivationletterbot.kafka.KafkaProducer;
import com.test.motivationletterbot.kafka.KafkaRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
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

import java.sql.Time;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.motivationletterbot.MessageConstants.*;
import static com.test.motivationletterbot.entity.BotCommandEnum.*;

import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;

@Slf4j
@Component
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final KafkaProducer kafkaProducer;
    private final long creatorId;

    public MotivationLetterBot(
            BotProperties botProperties,
            KafkaProducer kafkaProducer,
            TelegramClient telegramClient,
            BareboneToggle toggle,
            ConcurrentHashMap<Long, UserSession> userSessions,
            SilentSender silent,
            InlineKeyboards inlineKeyboards) {
        super(
                telegramClient,
                botProperties.getName(),
                useInMemoryMapDB(),
                toggle
        );
        addExtensions(new Abilities(this, userSessions, silent, telegramClient, inlineKeyboards));
        this.botProperties = botProperties;
        this.kafkaProducer = kafkaProducer;
        this.creatorId = botProperties.getBotCreatorId();
    }

    private static DBContext useInMemoryMapDB() {
        String useInMemory = System.getProperty("bot.use-inmemory-db",
                System.getenv("USE_INMEMORY_MAPDB"));

        String dbName;
        if ("true".equalsIgnoreCase(useInMemory)) {
            // Use a unique in-memory DB name for each run, stored in a temp folder
            dbName = "./tmpdb/inmem-" + UUID.randomUUID();
        } else {
            dbName = "./tmpdb/MotivationLetterBot-" + System.currentTimeMillis();

        }
        return MapDBContext.offlineInstance(dbName);
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
        if (!checkGlobalFlags(update)) {
            log.warn("It make no sense to send photos");
            return;
        }
        super.consume(update);

        if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();

            if (call_data.equals("MOTIVATION")) {

            }
        }

        Optional.ofNullable(update.getMessage()).ifPresent(message -> {

        });

    }

    @Override
    public boolean checkGlobalFlags(Update update) {
        return Flag.TEXT.test(update) || Flag.CALLBACK_QUERY.test(update);
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
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("\n\n\n{}: Registered bot '{}' (token: {}) running state is: {}", new Date(), botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }
}