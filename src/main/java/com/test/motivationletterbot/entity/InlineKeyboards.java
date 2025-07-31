package com.test.motivationletterbot.entity;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Component
public class InlineKeyboards {
    public List<InlineKeyboardRow> startKeyboard() {
        return List.of(
                createRow("Motivation", "MOTIVATION"),
                createRow("Role description", "ROLE_DESCRIPTION"),
                createRow("Additional information", "ADDITIONAL_INFORMATION")
        );
    }

    private InlineKeyboardRow createRow(String text, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        return new InlineKeyboardRow(button);  // This is InlineKeyboardRow (which is just List<InlineKeyboardButton>)
    }
}
