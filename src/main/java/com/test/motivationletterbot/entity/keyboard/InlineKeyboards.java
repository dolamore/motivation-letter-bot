package com.test.motivationletterbot.entity.keyboard;

import com.test.motivationletterbot.entity.textentry.TextEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Collection;
import java.util.List;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

@Component
@Getter
@Slf4j
public class InlineKeyboards {
    private final List<InlineKeyboardRow> startKeyboard = startKeyboard();
    // keep emptyKeyboard truly empty (no empty row) to avoid sending an empty row to Telegram
    private final List<InlineKeyboardRow> emptyKeyboard = List.of();
    private final List<InlineKeyboardRow> returnMenuKeyboard = List.of(RETURN_MENU_ROW.getRow());
    private final List<InlineKeyboardRow> greetingKeyboard = List.of(GREETING_ROW.getRow());
    private final List<InlineKeyboardRow> continueKeyboard = List.of(SUBMIT_ROW.getRow(), RETURN_MENU_ROW.getRow());
    private final List<InlineKeyboardRow> restartKeyboard = List.of(GREETING_ROW.getRow());

    private List<InlineKeyboardRow> startKeyboard() {
        return List.of(ROLE_DESCRIPTION_ROW.getRow(), MOTIVATION_ROW.getRow(), ADDITIONAL_INFORMATION_ROW.getRow());
    }

    public List<InlineKeyboardRow> getMenuKeyboard(Collection<TextEntry> textEntries) {
        var keyboard = new java.util.ArrayList<>(emptyKeyboard);
        log.warn("Building menu keyboard: initialRows={} entries={}", keyboard.size(), textEntries == null ? 0 : textEntries.size());
        if (textEntries != null) {
            textEntries.forEach(textEntry -> {
                log.warn("Building menu keyboard: textEntry={}", textEntry);
                if (!textEntry.isComplete()) {
                    keyboard.add(textEntry.getKeyboardRow());
                }
            });
        }
        log.warn("Built menu keyboard: finalRows={}", keyboard.size());
        return keyboard;
    }
}
