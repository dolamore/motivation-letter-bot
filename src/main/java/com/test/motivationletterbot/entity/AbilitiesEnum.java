package com.test.motivationletterbot.entity;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.test.motivationletterbot.entity.CommandsEnum.*;
import static com.test.motivationletterbot.entity.BotMenuStateEnum.*;

@Getter
public enum AbilitiesEnum {
    START_ABILITY(
            "start",
            "Start motivation message creation",
            MAIN.getStateCommands(),
            UserSession::startSession,
            UserSession::startingMessage,
            UserSession::startKeyboard
    ),
    MENU_ABILITY(
            "menu",
            "Show main menu",
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::startSession,
            UserSession::returnMessage,
            UserSession::startKeyboard
    ),
    START_MOTIVATION_ABILITY(
            "start_m",
            "Write new motivation",
            EnumSet.of(END_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::startMotivationWriting,
            UserSession::returnMessage,
            null
    ),
    CONTINUE_MOTIVATION_ABILITY(
            "continue_m",
            "Continue writing motivation",
            EnumSet.of(END_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::startMotivationWriting,
            UserSession::returnMessage,
            null
    ),
    RECORD_MOTIVATION_ABILITY(
            "end_m",
            "End motivation writing",
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::completeMotivation,
            UserSession::returnMessage,
            null
    ),
    END_MOTIVATION_ABILITY(
            "end_m",
            "End motivation writing",
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::completeMotivation,
            UserSession::returnMessage,
            null
    ),
    START_ROLE_DESCRIPTION_ABILITY(
            "start_rd",
            "Start role description writing",
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::resetVacancy,
            UserSession::returnMessage,
            null
    ),
    END_ROLE_DESCRIPTION_ABILITY(
            "end_rd",
            "End role description writing",
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            UserSession::completeVacancy,
            UserSession::returnMessage,
            null
    );

    private final String abilityName;
    private final String info;
    private final List<BotCommand> commands;
    private final Consumer<UserSession> sessionAction;
    private final Function<UserSession, String> message;
    private final Function<UserSession, List<InlineKeyboardRow>> inlineKeyboardSupplier;

    AbilitiesEnum(
            String abilityName,
            String info,
            EnumSet<CommandsEnum> commands,
            Consumer<UserSession> sessionAction,
            Function<UserSession, String> message,
            Function<UserSession, List<InlineKeyboardRow>> inlineKeyboardSupplier) {
        this.commands = commands.stream().map(CommandsEnum::getBotCommand).toList();
        this.abilityName = abilityName;
        this.info = info;
        this.sessionAction = sessionAction;
        this.message = message;
        this.inlineKeyboardSupplier = inlineKeyboardSupplier != null ? inlineKeyboardSupplier : chatId -> null;
    }
}