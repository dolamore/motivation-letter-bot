package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.UserSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.test.motivationletterbot.entity.textentry.TextEntryType.*;
import static com.test.motivationletterbot.entity.commands.CommandsEnum.*;

@Getter
@AllArgsConstructor
public enum AbilitiesEnum {
    START_ABILITY(
            new AbilityMeta(START_COMMAND),
            AbilityBehavior.createStartBehavior()
    ),
    RESTART_ABILITY(
            new AbilityMeta(RESTART_COMMAND),
            AbilityBehavior.createRestartBehavior()
    ),
    MENU_ABILITY(
            new AbilityMeta(MENU_COMMAND),
            AbilityBehavior.createMenuBehavior()
    ),
    GENERATE_ABILITY(
            new AbilityMeta(GENERATE_COMMAND),
            AbilityBehavior.createGenerateBehavior()
    ),


    WRITE_MOTIVATION_ABILITY(
            new AbilityMeta(WRITE_MOTIVATION_COMMAND),
            AbilityBehavior.createWriteBehavior(MOTIVATION_TEXT_ENTRY)
    ),
    WRITE_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(WRITE_ROLE_DESCRIPTION_COMMAND),
            AbilityBehavior.createWriteBehavior(VACANCY_TEXT_ENTRY)
    ),
    WRITE_ADDITIONAL_INFORMATION_ABILITY(
            new AbilityMeta(WRITE_ADDITIONAL_INFORMATION_COMMAND),
            AbilityBehavior.createWriteBehavior(ADDITIONAL_INFORMATION_TEXT_ENTRY)
    ),


    CONTINUE_ABILITY(
            new AbilityMeta(CONTINUE_COMMAND),
            AbilityBehavior.createContinueBehavior()
    ),
    RECORD_TEXT_ENTRY_ABILITY(
            new AbilityMeta(SUBMIT_COMMAND),
            AbilityBehavior.createRecordBehavior()
    );


    private final AbilityMeta meta;
    private final AbilityBehavior behavior;

    public String getAbilityName() {
        return meta.name();
    }

    public String getInfo() {
        return meta.info();
    }

    public Consumer<UserSession> getSessionAction() {
        return behavior.sessionAction();
    }

    public Function<UserSession, String> getMessage() {
        return behavior.message();
    }

    public Function<UserSession, List<InlineKeyboardRow>> getInlineKeyboardSupplier() {
        return behavior.inlineKeyboardSupplier();
    }
}