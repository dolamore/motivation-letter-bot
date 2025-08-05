package com.test.motivationletterbot.entity;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.test.motivationletterbot.MessageConstants.STARTING_MESSAGE;
import static com.test.motivationletterbot.entity.CommandsEnum.*;

@Getter
public enum AbilitiesEnum {
    START_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "start",
            "Start motivation message creation",
            UserSession::startSession,
            STARTING_MESSAGE,
            InlineKeyboards::startKeyboard
    ),
    MENU_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "menu",
            "Show main menu",
            UserSession::startSession,
            "Main menu displayed",
            InlineKeyboards::startKeyboard
    ),
    START_MOTIVATION_ABILITY(
            EnumSet.of(END_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "start_m",
            "Start motivation writing",
            UserSession::resetMotivation,
            "Please provide your motivation text!",
            null
    ),
    END_MOTIVATION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "end_m",
            "End motivation writing",
            UserSession::completeMotivation,
            "Your motivation text was successfully recorded",
            null
    ),
    START_ROLE_DESCRIPTION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "start_rd",
            "Start role description writing",
            UserSession::resetVacancy,
            "Please provide your role description!",
            null
    ),
    END_ROLE_DESCRIPTION_ABILITY(
            EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND),
            "end_rd",
            "End role description writing",
            UserSession::completeVacancy,
            "Your role description was successfully recorded",
            null
    );

    private final List<BotCommand> commands;
    private final String abilityName;
    private final String info;
    private final Consumer<UserSession> sessionAction;
    private final String message;
    private final Function<InlineKeyboards, List<InlineKeyboardRow>> inlineKeyboardSupplier;

    AbilitiesEnum(EnumSet<CommandsEnum> commands, String abilityName, String info,
                  Consumer<UserSession> sessionAction, String message,
                  Function<InlineKeyboards, List<InlineKeyboardRow>> inlineKeyboardSupplier) {
        this.commands = commands.stream().map(CommandsEnum::getBotCommand).toList();
        this.abilityName = abilityName;
        this.info = info;
        this.sessionAction = sessionAction;
        this.message = message;
        this.inlineKeyboardSupplier = inlineKeyboardSupplier != null ? inlineKeyboardSupplier : chatId -> null;
    }
}