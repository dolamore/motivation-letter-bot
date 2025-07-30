package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.kafka.KafkaProducer;
import com.test.motivationletterbot.kafka.KafkaRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.motivationletterbot.MessageConstants.*;

@Slf4j
@Component
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final KafkaProducer kafkaProducer;
    private final ConcurrentHashMap<Long, StringBuilder> motivation = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, StringBuilder> vacancy = new ConcurrentHashMap<>();
    private boolean messageIsComplete = false;
    private final long creatorId;
    private final Abilities abilities;

    public MotivationLetterBot(
            BotProperties botProperties,
            KafkaProducer kafkaProducer, TelegramClient telegramClient, Abilities abilities) {
        super(telegramClient, botProperties.getName());
        this.botProperties = botProperties;
        this.kafkaProducer = kafkaProducer;
        this.creatorId = botProperties.getBotCreatorId();
        this.abilities = abilities;
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
        super.consume(update);
        var message = update.getMessage();
        if (message != null && message.hasText()) {
            String messageText = message.getText();
            long chat_id = message.getChatId();


            if (messageIsComplete) {
                sendMessage(chat_id, PROCESSING_MESSAGE);
                sendToKafka(chat_id, messageText);
            }
        }
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
        // logic to run before bot registration
        log.warn("MotivationLetterBot bean constructed and dependencies injected. Running pre-registration logic.");
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("Registered bot '{}' (token: {}) running state is: {}", botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }

    public Ability sayHelloWorld() {
        return abilities.sayHelloWorld(silent);
    }

    public Ability saysHelloWorldToFriend() {
        return abilities.saysHelloWorldToFriend(silent);
    }
}
