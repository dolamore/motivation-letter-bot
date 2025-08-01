package com.test.motivationletterbot.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
@Service
public class AbilityService {
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final SilentSender silent;
    private final TelegramClient telegramClient;
    private final InlineKeyboards inlineKeyboards;

    public AbilityService(
            ConcurrentHashMap<Long, UserSession> userSessions,
            SilentSender silent,
            TelegramClient telegramClient,
            InlineKeyboards inlineKeyboards) {
        this.userSessions = userSessions;
        this.silent = silent;
        this.telegramClient = telegramClient;
        this.inlineKeyboards = inlineKeyboards;
    }

    public Ability getAbility(BotCommandEnum commandEnum) {
        return Ability.builder()
                .name(commandEnum.name().toLowerCase())
                .info(commandEnum.getBotCommand().getDescription())
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    commandEnum.getSessionAction().accept(session);

                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(commandEnum.getMessage())
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                    .keyboard(commandEnum.getInlineKeyboardSupplier().apply(inlineKeyboards))
                                    .build()
                            )
                            .build();
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (Exception e) {
                        // fallback to silent if sending fails
                        silent.send(commandEnum.getMessage(), chatId);
                    }
                })
                .build();
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

    public void removeInlineKeyboard(Update update) {
        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = (int) update.getCallbackQuery().getMessage().getMessageId();

            EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .replyMarkup(null) // removes the inline keyboard
                    .build();

            try {
                telegramClient.execute(editMarkup);
            } catch (TelegramApiException e) {
                log.error("Failed to remove inline keyboard", e);
            }
        }
    }
}
