package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.MotivationLetterDataRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Component
public class MotivationLetterBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${bot.token}")
    private String botToken;
    private final TelegramClient telegramClient;
    private final MotivationLetterDataRepository motivationLetterDataRepository;

    @Autowired
    public MotivationLetterBot(@Value("${bot.token}") String botToken, MotivationLetterDataRepository motivationLetterDataRepository) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN must not be null or blank");
        }
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.motivationLetterDataRepository = motivationLetterDataRepository;
    }

    // Constructor for testability
    public MotivationLetterBot(String botToken, TelegramClient telegramClient, MotivationLetterDataRepository motivationLetterDataRepository) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN must not be null or blank");
        }
        this.botToken = botToken;
        this.telegramClient = telegramClient;
        this.motivationLetterDataRepository = motivationLetterDataRepository;
    }

    @Override
    public String getBotToken() {
        return botToken;
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

    private void sendMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private String buildResponseText(String messageText) {
        return "You said: " + messageText;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            String responseText = buildResponseText(messageText);

            SendMessage message = buildSendMessage(chat_id, responseText);
            sendMessage(message);
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}
