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


    WRITE_MOTIVATION_ABILITY(
            new AbilityMeta(WRITE_MOTIVATION_COMMAND),
            AbilityBehavior.createWriteBehavior(MOTIVATION_TEXT_ENTRY)
    ),
    CONTINUE_MOTIVATION_ABILITY(
            new AbilityMeta(CONTINUE_MOTIVATION_COMMAND),
            AbilityBehavior.createContinueBehavior(MOTIVATION_TEXT_ENTRY)
    ),
    RECORD_MOTIVATION_ABILITY(
            new AbilityMeta(SUBMIT_MOTIVATION_COMMAND),
            AbilityBehavior.createRecordBehavior(MOTIVATION_TEXT_ENTRY)
    ),


    WRITE_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(WRITE_ROLE_DESCRIPTION_COMMAND),
            AbilityBehavior.createWriteBehavior(VACANCY_TEXT_ENTRY)
    ),
    CONTINUE_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(CONTINUE_ROLE_DESCRIPTION_COMMAND),
            AbilityBehavior.createContinueBehavior(VACANCY_TEXT_ENTRY)
    ),
    RECORD_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(SUBMIT_ROLE_DESCRIPTION_COMMAND),
            AbilityBehavior.createRecordBehavior(VACANCY_TEXT_ENTRY)
    ),


    WRITE_ADDITIONAL_INFORMATION_ABILITY(
            new AbilityMeta(WRITE_ADDITIONAL_INFORMATION_COMMAND),
            AbilityBehavior.createWriteBehavior(ADDITIONAL_INFORMATION_TEXT_ENTRY)
    ),
    CONTINUE_ADDITIONAL_INFORMATION_ABILITY(
            new AbilityMeta(CONTINUE_ADDITIONAL_INFORMATION_COMMAND),
            AbilityBehavior.createContinueBehavior(ADDITIONAL_INFORMATION_TEXT_ENTRY)
    ),
    RECORD_ADDITIONAL_INFORMATION_ABILITY(
            new AbilityMeta(SUBMIT_ADDITIONAL_INFORMATION_COMMAND),
            AbilityBehavior.createRecordBehavior(ADDITIONAL_INFORMATION_TEXT_ENTRY)
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