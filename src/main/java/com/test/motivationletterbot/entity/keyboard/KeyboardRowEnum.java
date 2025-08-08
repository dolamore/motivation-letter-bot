package com.test.motivationletterbot.entity.keyboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Getter
@AllArgsConstructor
public enum KeyboardRowEnum {
    ROLE_DESCRIPTION_ROW(createRow("Role description", "write_rd")),

    MOTIVATION_ROW(createRow("Motivation", "write_m")),

    ADDITIONAL_INFORMATION_ROW(createRow("Company/product spec", "write_ai")),

    GREETING_ROW(createRow("Create letter", "new")),
    RETURN_MENU_ROW(createRow("Return to menu", "menu")),
    SUBMIT_ROW(createRow("Submit", "submit")),
    GENERATE_ROW(createRow("Generate Message", "generate"));

    private static InlineKeyboardRow createRow(String text, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        return new InlineKeyboardRow(button);
    }

    private final InlineKeyboardRow row;
}
