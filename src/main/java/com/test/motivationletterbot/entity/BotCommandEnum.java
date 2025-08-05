package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;


import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.test.motivationletterbot.MessageConstants.*;

@Getter
@AllArgsConstructor
public enum BotCommandEnum {
    START(
            new BotCommand("start", "Start writing motivation letter"),
            "start",
            "Start motivation message creation",
            UserSession::startSession,
            STARTING_MESSAGE,
            InlineKeyboards::startKeyboard
    ),
    START_MOTIVATION(
            new BotCommand("start_m", "Start motivation writing"),
            "start_m",
            "Start motivation writing",
            UserSession::resetMotivation,
            "Please provide your motivation text!",
            null
    ),
    END_MOTIVATION(
            new BotCommand("end_m", "End motivation writing"),
            "end_m",
            "End motivation writing",
            UserSession::completeMotivation,
            "Your motivation text was successfully recorded",
            null
    ),
    START_ROLE_DESCRIPTION(
            new BotCommand("start_rd", "Start role description writing"),
            "start_rd",
            "Start role description writing",
            UserSession::resetVacancy,
            "Please provide your role description!",
            null
    ),
    END_ROLE_DESCRIPTION(
            new BotCommand("end_rd", "End role description writing"),
            "end_rd",
            "End role description writing",
            UserSession::completeVacancy,
            "Your role description was successfully recorded",
            null
    );


    private final BotCommand botCommand;
    private final String abilityName;
    private final String info;
    private final Consumer<UserSession> sessionAction;
    private final String message;
    private final Function<InlineKeyboards, List<InlineKeyboardRow>> inlineKeyboardSupplier;
}
