package com.test.motivationletterbot.entity.keyboard;

import com.test.motivationletterbot.entity.TextEntry;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Collection;
import java.util.List;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

@Component
@Getter
public class InlineKeyboards {
    private final List<InlineKeyboardRow> startKeyboard;
    private final List<InlineKeyboardRow> emptyKeyboard = List.of(new InlineKeyboardRow());
    private final List<InlineKeyboardRow> returnMenuKeyboard = List.of(RETURN_MENU_ROW.getRow());

    public InlineKeyboards() {
        this.startKeyboard = startKeyboard();
    }

    private List<InlineKeyboardRow> startKeyboard() {
        return List.of(ROLE_DESCRIPTION_ROW.getRow(), MOTIVATION_ROW.getRow(), ADDITIONAL_INFORMATION_ROW.getRow());
    }

    public List<InlineKeyboardRow> getContinueKeyboard(TextEntry textEntry) {
        return List.of(textEntry.getSubmitKeyboardRow(), RETURN_MENU_ROW.getRow());
    }

    public List<InlineKeyboardRow> getMenuKeyboard(Collection<TextEntry> textEntries) {
        var keyboard = new java.util.ArrayList<>(emptyKeyboard);
        textEntries.forEach(textEntry -> {
            if (!textEntry.isComplete()) {
                keyboard.add(textEntry.getKeyboardRow());
            }
        });
        return keyboard;
    }
}
