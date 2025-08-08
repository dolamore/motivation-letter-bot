package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.textentry.TextEntryType;
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
                UserSession::writeMessage,
                UserSession::writingKeyboard
        );
    }

    public static AbilityBehavior createContinueBehavior() {
        return new AbilityBehavior(
                UserSession::continueWriting,
                UserSession::continueMessage,
                UserSession::continueKeyboard
        );
    }

    public static AbilityBehavior createRecordBehavior() {
        return new AbilityBehavior(
                UserSession::completeTextEntry,
                UserSession::menuMessage,
                UserSession::menuKeyboard
        );
    }

    public static AbilityBehavior createStartBehavior() {
        return new AbilityBehavior(
                (session) -> {
                },
                UserSession::greetingMessage,
                UserSession::greetingKeyboard
        );
    }

    public static AbilityBehavior createRestartBehavior() {
        return new AbilityBehavior(
                UserSession::startSession,
                UserSession::startingMessage,
                UserSession::startKeyboard
        );
    }

    public static AbilityBehavior createMenuBehavior() {
        return new AbilityBehavior(
                UserSession::returnToMenu,
                UserSession::menuMessage,
                UserSession::menuKeyboard
        );
    }
}