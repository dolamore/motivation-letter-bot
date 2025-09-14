package com.test.motivationletterbot.entity.textentry;

import com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

public record TextEntryTypeParams(KeyboardRowEnum keyboardRow,
                                  boolean isMandatory) {
    public static final TextEntryTypeParams MOTIVATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(MOTIVATION_ROW, false);
    public static final TextEntryTypeParams VACANCY_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ROLE_DESCRIPTION_ROW, true);
    public static final TextEntryTypeParams ADDITIONAL_INFORMATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ADDITIONAL_INFORMATION_ROW, false);
}
