package com.test.motivationletterbot.entity.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

import static com.test.motivationletterbot.entity.keyboard.KeyboardRowEnum.*;

@Component
public class InlineKeyboards {
    public List<InlineKeyboardRow> startKeyboard() {
        return List.of(
                MOTIVATION_ROW.getRow(),
                ROLE_DESCRIPTION_ROW.getRow(),
                ADDITIONAL_INFORMATION_ROW.getRow()
        );
    }
}
