package com.test.motivationletterbot.entity.keyboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Getter
@AllArgsConstructor
public enum KeyboardRowEnum {
    MOTIVATION_ROW(createRow("Motivation", "write_m")),
    ROLE_DESCRIPTION_ROW(createRow("Role description", "ROLE_DESCRIPTION")),
    ADDITIONAL_INFORMATION_ROW(createRow("Company/product spec", "ADDITIONAL_INFORMATION")),
    RETURN_MENU_ROW(createRow("Return to menu", "menu")),
    SUBMIT_ROW(createRow("Submit", "SUBMIT")),
    DROP_ROW(createRow("Drop", "DROP"));

    private static InlineKeyboardRow createRow(String text, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        return new InlineKeyboardRow(button);
    }

    private final InlineKeyboardRow row;
}
