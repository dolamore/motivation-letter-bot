package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.UserSession;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.test.motivationletterbot.entity.commands.CommandsEnum.*;

@Getter
public enum AbilitiesEnum {
    START_ABILITY(
            new AbilityMeta(START_COMMAND),
            new AbilityBehavior(UserSession::startSession, UserSession::startingMessage, UserSession::startKeyboard)
    ),
    RESTART_ABILITY(
            new AbilityMeta(RESTART_COMMAND),
            new AbilityBehavior(UserSession::startSession, UserSession::startingMessage, UserSession::startKeyboard)
    ),
    MENU_ABILITY(
            new AbilityMeta(MENU_COMMAND),
            new AbilityBehavior(UserSession::startSession, UserSession::startingMessage, UserSession::startKeyboard)
    ),


    WRITE_MOTIVATION_ABILITY(
            new AbilityMeta(WRITE_MOTIVATION_COMMAND),
            new AbilityBehavior(UserSession::startMotivationWriting, UserSession::writeMotivationMessage, UserSession::motivationKeyboard)
    ),
    CONTINUE_MOTIVATION_ABILITY(
            new AbilityMeta(CONTINUE_MOTIVATION_COMMAND),
            new AbilityBehavior(UserSession::continueMotivationWriting, UserSession::continueMotivationMessage, UserSession::continueMotivationKeyboard)
    ),
    RECORD_MOTIVATION_ABILITY(
            new AbilityMeta(SUBMIT_MOTIVATION_COMMAND),
            new AbilityBehavior(UserSession::completeMotivation, UserSession::returnMessage, null)
    ),
    DROP_MOTIVATION_ABILITY(
            new AbilityMeta(DROP_MOTIVATION_COMMAND),
            new AbilityBehavior(UserSession::completeMotivation, UserSession::returnMessage, null)
    ),


    START_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(WRITE_ROLE_DESCRIPTION_COMMAND),
            new AbilityBehavior(UserSession::resetVacancy, UserSession::returnMessage, null)
    ),
    CONTINUE_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(SUBMIT_ROLE_DESCRIPTION_COMMAND),
            new AbilityBehavior(UserSession::completeVacancy, UserSession::returnMessage, null)
    ),
    RECORD_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(SUBMIT_ROLE_DESCRIPTION_COMMAND),
            new AbilityBehavior(UserSession::completeVacancy, UserSession::returnMessage, null)
    ),
    DROP_ROLE_DESCRIPTION_ABILITY(
            new AbilityMeta(SUBMIT_ROLE_DESCRIPTION_COMMAND),
            new AbilityBehavior(UserSession::completeVacancy, UserSession::returnMessage, null)
    );


//    START_ADDITIONAL_INFORMATION_ABILITY(
//            new AbilityMeta(WRITE_ADDITIONAL_INFORMATION_COMMAND),
//            new AbilityBehavior(UserSession::resetAdditionalInformation, UserSession::returnMessage, null)
//    ),
//    CONTINUE_ADDITIONAL_INFORMATION_ABILITY(
//            new AbilityMeta(CONTINUE_ADDITIONAL_INFORMATION_COMMAND),
//            new AbilityBehavior(UserSession::completeAdditionalInformation, UserSession::returnMessage, null)
//    ),
//    RECORD_ADDITIONAL_INFORMATION_ABILITY(
//            new AbilityMeta(SUBMIT_ADDITIONAL_INFORMATION_COMMAND),
//            new AbilityBehavior(UserSession::completeAdditionalInformation, UserSession::returnMessage, null)
//    ),
//    DROP_ADDITIONAL_INFORMATION_ABILITY(
//            new AbilityMeta(DROP_ADDITIONAL_INFORMATION_COMMAND),
//            new AbilityBehavior(UserSession::completeAdditionalInformation, UserSession::returnMessage, null)
//    );

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

    AbilitiesEnum(
            AbilityMeta meta,
            AbilityBehavior behavior) {
        this.meta = meta;
        this.behavior = behavior;
    }
}