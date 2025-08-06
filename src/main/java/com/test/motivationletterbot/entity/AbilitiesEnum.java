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
            MAIN.getStateCommands(),
            "start",
            "Start motivation message creation",
            UserSession::startSession,
            UserSession::returnMessage,
            InlineKeyboards::startKeyboard
    ),
    MENU_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "menu",
            "Show main menu",
            UserSession::startSession,
            UserSession::returnMessage,
            InlineKeyboards::startKeyboard
    ),
    START_MOTIVATION_ABILITY(
            EnumSet.of(END_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "start_m",
            "Write new motivation",
            UserSession::startMotivationWriting,
            UserSession::returnMessage,
            null
    ),
    CONTINUE_MOTIVATION_ABILITY(
            EnumSet.of(END_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "continue_m",
            "Continue writing motivation",
            UserSession::startMotivationWriting,
            UserSession::returnMessage,
            null
    ),
    RECORD_MOTIVATION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "end_m",
            "End motivation writing",
            UserSession::completeMotivation,
            UserSession::returnMessage,
            null
    ),
    END_MOTIVATION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "end_m",
            "End motivation writing",
            UserSession::completeMotivation,
            UserSession::returnMessage,
            null
    ),
    START_ROLE_DESCRIPTION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "start_rd",
            "Start role description writing",
            UserSession::resetVacancy,
            UserSession::returnMessage,
            null
    ),
    END_ROLE_DESCRIPTION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "end_rd",
            "End role description writing",
            UserSession::completeVacancy,
            UserSession::returnMessage,
            null
    );

    private final List<BotCommand> commands;
    private final String abilityName;
    private final String info;
    private final Consumer<UserSession> sessionAction;
    private final Function<UserSession, String> message;
    private final Function<InlineKeyboards, List<InlineKeyboardRow>> inlineKeyboardSupplier;

    AbilitiesEnum(EnumSet<CommandsEnum> commands, String abilityName, String info,
                  Consumer<UserSession> sessionAction, Function<UserSession, String> message,
                  Function<InlineKeyboards, List<InlineKeyboardRow>> inlineKeyboardSupplier) {
        this.commands = commands.stream().map(CommandsEnum::getBotCommand).toList();
        this.abilityName = abilityName;
        this.info = info;
        this.sessionAction = sessionAction;
        this.message = message;
        this.inlineKeyboardSupplier = inlineKeyboardSupplier != null ? inlineKeyboardSupplier : chatId -> null;
    }
}