package com.test.motivationletterbot.util;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public class BotUtils {
    public static boolean isCommand(Message message) {
        if (message.getEntities() != null) {
            return message.getEntities().stream()
                    .anyMatch(e -> "bot_command".equals(e.getType()) && e.getOffset() == 0);
        }
        String text = message.getText();
        return text != null && text.trim().startsWith("/");
    }
}

