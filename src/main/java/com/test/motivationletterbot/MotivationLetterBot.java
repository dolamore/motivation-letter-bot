package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.kafka.MotivationLetterKafkaProducer;
import com.test.motivationletterbot.kafka.MotivationLetterRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.motivationletterbot.MessageConstants.*;

@Slf4j
@Component
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final MotivationLetterKafkaProducer kafkaProducer;
    private final ConcurrentHashMap<Long, StringBuilder> motivation = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, StringBuilder> vacancy = new ConcurrentHashMap<>();
    private boolean messageIsComplete = false;
    private final long creatorId;

    public MotivationLetterBot(
            BotProperties botProperties,
            MotivationLetterKafkaProducer kafkaProducer, TelegramClient telegramClient) {
        super(telegramClient, botProperties.getName());
        this.botProperties = botProperties;
        this.kafkaProducer = kafkaProducer;
        this.creatorId = botProperties.getBotCreatorId();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
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
        if (message != null && message.hasText()) {
            String messageText = message.getText();
            long chat_id = message.getChatId();
            if (messageText.equalsIgnoreCase("/start")) {
                motivation.put(chat_id, new StringBuilder());
                vacancy.put(chat_id, new StringBuilder());
                messageIsComplete = false;
                sendMessage(chat_id, "started");
                return;
            }

            if (messageText.equalsIgnoreCase("/stop")) {
                motivation.remove(chat_id);
                vacancy.remove(chat_id);
                messageIsComplete = false;
                sendMessage(chat_id, "stopped");
                return;
            }

            if (messageIsComplete) {
                sendMessage(chat_id, PROCESSING_MESSAGE);
                sendToKafka(chat_id, messageText);
            }
        }
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    void sendToKafka(long chatId, String messageText) {
        CompletableFuture<SendResult<String, MotivationLetterRequest>> future = kafkaProducer.sendRequest(
                new MotivationLetterRequest(chatId, messageText)
        );
        future.whenComplete((result, e) -> {
            if (e != null) {
                log.error("Failed to send Kafka request", e);
                sendMessage(chatId, ERROR_MESSAGE);
            }
        });
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("Registered bot '{}' (token: {}) running state is: {}", botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }
}
