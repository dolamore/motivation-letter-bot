package com.test.motivationletterbot.entity;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BotAbilityCommandService {
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final SilentSender silent;
    private final TelegramClient telegramClient;

    public BotAbilityCommandService(ConcurrentHashMap<Long, UserSession> userSessions, SilentSender silent, TelegramClient telegramClient) {
        this.userSessions = userSessions;
        this.silent = silent;
        this.telegramClient = telegramClient;
    }

    public Ability getAbility(BotAbilityCommandEnum commandEnum) {
        return Ability.builder()
                .name(commandEnum.name().toLowerCase())
                .info(commandEnum.getBotCommand().getDescription())
                .privacy(org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC)
                .locality(org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    commandEnum.getSessionAction().accept(session);
                    silent.send(commandEnum.getMessage(), chatId);
                })
                .build();
    }

    private SetMyCommands getCommandsSet(BotAbilityCommandEnum... enums) {
        List<BotCommand> commands = new ArrayList<>();
        for (BotAbilityCommandEnum e : enums) {
            commands.add(e.getBotCommand());
        }

        return SetMyCommands.builder()
                .commands(commands)
                .build();
    }

    public void setBotCommands(BotAbilityCommandEnum... enums) {
        SetMyCommands setMyCommands = getCommandsSet(enums);
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to set bot commands", e);
        }
    }
}

