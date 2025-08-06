package com.test.motivationletterbot.entity.keyboard;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

@Component
@Getter
public class InlineKeyboards {
    private final List<InlineKeyboardRow> startKeyboard;
    private final List<InlineKeyboardRow> emptyKeyboard;
    private final List<InlineKeyboardRow> completedMotivationKeyboard;
    private final List<InlineKeyboardRow> emptyMotivationKeyboard;
    private final List<InlineKeyboardRow> continueMotivationKeyboard;

    public InlineKeyboards() {
        this.startKeyboard = startKeyboard();
        this.emptyKeyboard = emptyKeyboard();
        this.completedMotivationKeyboard = completedMotivationKeyboard();
        this.emptyMotivationKeyboard = emptyMotivationKeyboard();
        this.continueMotivationKeyboard = continueMotivationKeyboard();
    }

    private List<InlineKeyboardRow> startKeyboard() {
        return List.of(
                MOTIVATION_ROW.getRow(),
                ROLE_DESCRIPTION_ROW.getRow(),
                ADDITIONAL_INFORMATION_ROW.getRow()
        );
    }

    private List<InlineKeyboardRow> completedMotivationKeyboard() {
        return List.of(RETURN_MENU_ROW.getRow(), SUBMIT_ROW.getRow());
    }

    private List<InlineKeyboardRow> emptyMotivationKeyboard() {
        return List.of(RETURN_MENU_ROW.getRow());
    }

    private List<InlineKeyboardRow> emptyKeyboard() {
        return List.of(new InlineKeyboardRow());
    }

    private List<InlineKeyboardRow> continueMotivationKeyboard() {
        return List.of(SUBMIT_ROW.getRow(), RETURN_MENU_ROW.getRow());
    }
}
