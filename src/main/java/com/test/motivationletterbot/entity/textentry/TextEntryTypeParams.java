package com.test.motivationletterbot.entity.textentry;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

public record TextEntryTypeParams(InlineKeyboardRow keyboardRow,
                                  boolean isMandatory) {
    public static final TextEntryTypeParams MOTIVATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(MOTIVATION_ROW.getRow(), false);
    public static final TextEntryTypeParams VACANCY_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ROLE_DESCRIPTION_ROW.getRow(), true);
    public static final TextEntryTypeParams ADDITIONAL_INFORMATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ADDITIONAL_INFORMATION_ROW.getRow(), false);
}
