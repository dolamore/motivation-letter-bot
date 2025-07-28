package com.test.motivationletterbot;

public interface TelegramMessageSender {
    void sendMessageToUser(Long chatId, String text);
}

