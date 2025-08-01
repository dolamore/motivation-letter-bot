package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.MotivationLetterBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.test.motivationletterbot.entity.BotCommandEnum.*;
import static com.test.motivationletterbot.entity.BotCommandEnum.END_ROLE_DESCRIPTION;
import static com.test.motivationletterbot.entity.BotCommandEnum.START_ROLE_DESCRIPTION;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class Abilities implements AbilityExtension {
    private AbilityBot extensionUser;
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final SilentSender silent;
    private final TelegramClient telegramClient;
    private final InlineKeyboards inlineKeyboards;

    public Abilities(
            MotivationLetterBot motivationLetterBot,
            ConcurrentHashMap<Long, UserSession> userSessions,
            SilentSender silent,
            TelegramClient telegramClient,
            InlineKeyboards inlineKeyboards) {
        this.userSessions = userSessions;
        this.silent = silent;
        this.telegramClient = telegramClient;
        this.inlineKeyboards = inlineKeyboards;
    }

    public Ability startMessageWriting() {
        return getAbility(START).get();
    }

    public Ability startMotivationWriting() {
        return getAbility(START_MOTIVATION).get();
    }

    public Ability endMotivationWriting() {
        return getAbility(END_MOTIVATION).get();
    }

    public Ability startRoleDescriptionWriting() {
        return getAbility(START_ROLE_DESCRIPTION).get();
    }

    public Ability endRoleDescriptionWriting() {
        return getAbility(END_ROLE_DESCRIPTION).get();
    }

    private Supplier<Ability> getAbility(BotCommandEnum commandEnum) {
        return () -> Ability.builder()
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
                        Message sentMessage = telegramClient.execute(sendMessage);


                        if (session.getLastKeyboardMessageId() > 0) {
                            removeInlineKeyboard(chatId, session.getLastKeyboardMessageId());
                        }

                        int messageId = sentMessage.getMessageId();
                        session.setLastKeyboardMessageId(messageId);
                    } catch (Exception e) {
                        // fallback to silent if sending fails
                        silent.send(commandEnum.getMessage(), chatId);
                    }
                })
                .build();
    }

    private void removeInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();

        try {
            telegramClient.execute(editMarkup);
        } catch (TelegramApiException e) {
            log.error("Failed to remove inline keyboard", e);
        }
    }
}
