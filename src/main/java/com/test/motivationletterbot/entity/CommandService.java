package com.test.motivationletterbot.entity;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommandService {
    private final TelegramClient telegramClient;

    public CommandService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    private SetMyCommands getCommandsSet(BotCommandEnum... enums) {
        List<BotCommand> commands = new ArrayList<>();
        for (BotCommandEnum e : enums) {
            commands.add(e.getBotCommand());
        }

        return SetMyCommands.builder()
                .commands(commands)
                .build();
    }

    public void setBotCommands(BotCommandEnum... enums) {
        SetMyCommands setMyCommands = getCommandsSet(enums);
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to set bot commands", e);
        }
    }
}
