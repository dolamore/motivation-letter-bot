package com.test.motivationletterbot.entity;

import lombok.Getter;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public enum BotAbilityCommandEnum {
    START(
        new BotCommand("start", "Start the bot"),
        "start",
        "Start the bot",
        session -> {},
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

    private static ConcurrentHashMap<Long, UserSession> userSessions;
    private static SilentSender silent;
    private static TelegramClient telegramClient;

    @Getter
    private final BotCommand botCommand;
    private final String abilityName;
    private final String info;
    private final Consumer<UserSession> sessionAction;
    private final String message;

    BotAbilityCommandEnum(BotCommand botCommand, String abilityName, String info, Consumer<UserSession> sessionAction, String message) {
        this.botCommand = botCommand;
        this.abilityName = abilityName;
        this.info = info;
        this.sessionAction = sessionAction;
        this.message = message;
    }

    public static void initCommands(ConcurrentHashMap<Long, UserSession> sessions, SilentSender sender, TelegramClient telegramClient) {
        BotAbilityCommandEnum.userSessions = sessions;
        BotAbilityCommandEnum.silent = sender;
        BotAbilityCommandEnum.telegramClient = telegramClient;

    }

    public Ability getAbility() {
        return Ability.builder()
                .name(abilityName)
                .info(info)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    sessionAction.accept(session);
                    silent.send(message, chatId);
                })
                .build();
    }

    public static List<BotCommand> getCommandsList(BotAbilityCommandEnum... enums) {
        List<BotCommand> commands = new ArrayList<>();
        for (BotAbilityCommandEnum e : enums) {
            commands.add(e.getBotCommand());
        }
        return commands;
    }

    public static void setBotCommands(TelegramClient telegramClient, BotAbilityCommandEnum... enums) {
        List<BotCommand> commands = getCommandsList(enums);
        SetMyCommands setMyCommands = SetMyCommands.builder()
                .commands(commands)
                .build();
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to set bot commands", e);
        }
    }
}
