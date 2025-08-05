package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@AllArgsConstructor
public class CommandService {
    private final TelegramClient telegramClient;


    private SetMyCommands getSetMyCommands(Long chatId, List<BotCommand> commands) {
        return SetMyCommands.builder()
                .commands(commands)
                .scope(new BotCommandScopeChat(chatId.toString()))
                .build();
    }

    public void setBotCommands(Long chatId, List<BotCommand> commands) {
        SetMyCommands setMyCommands = getSetMyCommands(chatId, commands);
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to set bot commands", e);
        }
    }
}
