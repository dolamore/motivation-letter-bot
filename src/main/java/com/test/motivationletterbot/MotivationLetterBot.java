package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.MotivationLetterData;
import com.test.motivationletterbot.entity.MotivationLetterDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private final String botToken;
    private final TelegramClient telegramClient;

    @Autowired
    private MotivationLetterDataRepository motivationLetterDataRepository;

    @Autowired
    public MotivationLetterBot(Environment env) {
        String botToken = env.getProperty("BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN environment variable is not set");
        }
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    // Constructor for testability
    public MotivationLetterBot(String botToken, TelegramClient telegramClient) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN must not be null or blank");
        }
        this.botToken = botToken;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return null;
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

    private MotivationLetterData buildMotivationLetterData(String roleDescription, String motivation, String generatedText) {
        return new MotivationLetterData(roleDescription, motivation, generatedText, java.time.LocalDateTime.now());
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            String responseText = buildResponseText(messageText);

            // Example: using messageText as roleDescription, motivation, and generatedText
            MotivationLetterData data = buildMotivationLetterData(messageText, null, responseText);
            motivationLetterDataRepository.save(data);

            SendMessage message = buildSendMessage(chat_id, responseText);
            sendMessage(message);
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}
