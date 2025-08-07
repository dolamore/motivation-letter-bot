package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.TextEntryType;
import com.test.motivationletterbot.entity.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public record AbilityBehavior(
        Consumer<UserSession> sessionAction,
        Function<UserSession, String> message,
        Function<UserSession, List<InlineKeyboardRow>> inlineKeyboardSupplier
) {

    public static AbilityBehavior createWriteBehavior(TextEntryType type) {
        return new AbilityBehavior(
                (session) -> session.startWriting(type),
                (session) -> session.writeMessage(type),
                UserSession::writingKeyboard
        );
    }

    public static AbilityBehavior createContinueBehavior(TextEntryType type) {
        return new AbilityBehavior(
                (session) -> session.continueWriting(type),
                (session) -> session.continueMessage(type),
                (session) -> session.continueKeyboard(type)
        );
    }

    public static AbilityBehavior createRecordBehavior(TextEntryType type) {
        return new AbilityBehavior(
                (session) -> session.completeTextEntry(type),
                UserSession::menuMessage,
                UserSession::menuKeyboard
        );
    }

    public static AbilityBehavior createStartBehavior() {
        return new AbilityBehavior(
            UserSession::startSession,
            UserSession::startingMessage,
            UserSession::startKeyboard
        );
    }
}
