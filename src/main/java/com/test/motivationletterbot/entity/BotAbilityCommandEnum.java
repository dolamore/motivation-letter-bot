package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.function.Consumer;

@Getter
@AllArgsConstructor
public enum BotAbilityCommandEnum {
    START(
            new BotCommand("start", "Start the bot"),
            "start",
            "Start motivation message creation",
            session -> {
            },
            "Welcome! Use /help to see available commands."
    ),
    START_MOTIVATION(
            new BotCommand("start_m", "Start motivation writing"),
            "start_m",
            "Start motivation writing",
            UserSession::resetMotivation,
            "Please provide your motivation text!"
    ),
    END_MOTIVATION(
            new BotCommand("end_m", "End motivation writing"),
            "end_m",
            "End motivation writing",
            UserSession::completeMotivation,
            "Your motivation text was successfully recorded"
    ),
    START_ROLE_DESCRIPTION(
            new BotCommand("start_rd", "Start role description writing"),
            "start_rd",
            "Start role description writing",
            UserSession::resetVacancy,
            "Please provide your role description!"
    ),
    END_ROLE_DESCRIPTION(
            new BotCommand("end_rd", "End role description writing"),
            "end_rd",
            "End role description writing",
            UserSession::completeVacancy,
            "Your role description was successfully recorded"
    );


    private final BotCommand botCommand;
    private final String abilityName;
    private final String info;
    private final Consumer<UserSession> sessionAction;
    private final String message;
}
