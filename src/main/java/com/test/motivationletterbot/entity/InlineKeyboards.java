package com.test.motivationletterbot.entity;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component
public class InlineKeyboards {
    public InlineKeyboardRow startKeyboard() {
        return new InlineKeyboardRow(InlineKeyboardButton
                .builder()
                .text("Update message text")
                .callbackData("update_msg_text")
                .build()
        );
    }
}
