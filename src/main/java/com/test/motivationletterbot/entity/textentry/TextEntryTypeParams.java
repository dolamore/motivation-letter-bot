package com.test.motivationletterbot.entity.textentry;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

public record TextEntryTypeParams(InlineKeyboardRow keyboardRow,
                                  InlineKeyboardRow submitKeyboardRow,
                                  String continueAbilityKey, boolean isMandatory) {
    public static final TextEntryTypeParams MOTIVATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(MOTIVATION_ROW.getRow(), SUBMIT_MOTIVATION_ROW.getRow(), "cont_m", false);
    public static final TextEntryTypeParams VACANCY_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ROLE_DESCRIPTION_ROW.getRow(), SUBMIT_ROLE_DESCRIPTION_ROW.getRow(), "cont_rd", true);
    public static final TextEntryTypeParams ADDITIONAL_INFORMATION_TEXT_ENTRY_PARAMS = new TextEntryTypeParams(ADDITIONAL_INFORMATION_ROW.getRow(), SUBMIT_ADDITIONAL_INFORMATION_ROW.getRow(), "cont_ai", false);
}
