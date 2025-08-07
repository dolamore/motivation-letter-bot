package com.test.motivationletterbot.entity;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

public record TextEntryTypeParams(int commandLength, InlineKeyboardRow keyboardRow,
                                  InlineKeyboardRow submitKeyboardRow) {
    public static final TextEntryTypeParams MOTIVATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(6, MOTIVATION_ROW.getRow(), SUBMIT_MOTIVATION_ROW.getRow());
    public static final TextEntryTypeParams VACANCY_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(7, ROLE_DESCRIPTION_ROW.getRow(), SUBMIT_ROLE_DESCRIPTION_ROW.getRow());
    public static final TextEntryTypeParams ADDITIONAL_INFORMATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(0, ADDITIONAL_INFORMATION_ROW.getRow(), SUBMIT_ADDITIONAL_INFORMATION_ROW.getRow());
}
