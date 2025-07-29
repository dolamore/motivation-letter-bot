package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.kafka.MotivationLetterKafkaProducer;
import com.test.motivationletterbot.kafka.MotivationLetterRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

import static com.test.motivationletterbot.MessageConstants.*;

@Slf4j
@Component
public class MotivationLetterBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final BotProperties botProperties;
    private final MotivationLetterKafkaProducer kafkaProducer;

    public MotivationLetterBot(
            BotProperties botProperties,
            MotivationLetterKafkaProducer kafkaProducer) {
        this.botProperties = botProperties;
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
        this.kafkaProducer = kafkaProducer;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            sendMessage(chat_id, PROCESSING_MESSAGE);
            sendToKafka(chat_id, messageText);
        }
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
