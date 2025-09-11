package com.test.motivationletterbot.entity.keyboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Getter
@AllArgsConstructor
public enum KeyboardRowEnum {
    ROLE_DESCRIPTION_ROW("Role description", "write_rd"),

    MOTIVATION_ROW("Motivation", "write_m"),

    ADDITIONAL_INFORMATION_ROW("Company/product spec", "write_ai"),

    GREETING_ROW("Create new letter", "new"),
    RETURN_MENU_ROW("Return to menu", "menu"),
    SUBMIT_ROW("Submit", "submit"),
    GENERATE_ROW("Generate Message", "generate");

    private final String text;
    private final String callbackData;

    /**
     * Build and return a fresh InlineKeyboardRow containing a single button.
     * This prevents sharing/mutation of the same InlineKeyboardRow instance across different keyboards.
     */
    public InlineKeyboardRow getRow() {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(this.text)
                .callbackData(this.callbackData)
                .build();
        return new InlineKeyboardRow(button);
    }
}
