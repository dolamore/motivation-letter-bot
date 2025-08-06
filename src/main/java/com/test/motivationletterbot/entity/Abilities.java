package com.test.motivationletterbot.entity;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


import static com.test.motivationletterbot.MessageConstants.ERROR_MESSAGE;
import static com.test.motivationletterbot.entity.AbilitiesEnum.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class Abilities implements AbilityExtension {
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final SilentSender silent;
    private final TelegramClient telegramClient;
    private final CommandService commandService;

    public Abilities(
            ConcurrentHashMap<Long, UserSession> userSessions,
            SilentSender silent,
            TelegramClient telegramClient,
            CommandService commandService) {
        this.commandService = commandService;
        this.userSessions = userSessions;
        this.silent = silent;
        this.telegramClient = telegramClient;
    }

    public Ability startMessageWriting() {
        return getAbility(START_ABILITY).get();
    }

    public Ability restartMessageWriting() {
        return getAbility(RESTART_ABILITY).get();
    }

    public Ability startMotivationWriting() {
        return getAbility(START_MOTIVATION_ABILITY).get();
    }

    public Ability endMotivationWriting() {
        return getAbility(RECORD_MOTIVATION_ABILITY).get();
    }

    public Ability startRoleDescriptionWriting() {
        return getAbility(START_ROLE_DESCRIPTION_ABILITY).get();
    }

    public Ability endRoleDescriptionWriting() {
        return getAbility(END_ROLE_DESCRIPTION_ABILITY).get();
    }

    private Supplier<Ability> getAbility(AbilitiesEnum state) {
        return () -> Ability.builder()
                .name(state.getAbilityName())
                .info(state.getInfo())
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    state.getSessionAction().accept(session);

                    commandService.setBotCommands(chatId, session.getBotCommands());

                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(state.getMessage().apply(session))
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                    .keyboard(
                                            Optional.ofNullable(state.getInlineKeyboardSupplier())
                                                    .map(supplier -> supplier.apply(session))
                                                    .filter(Objects::nonNull)
                                                    .orElse(Collections.emptyList())
                                    )
                                    .build()
                            )
                            .build();


                    try {
                        Message sentMessage = telegramClient.execute(sendMessage);


                        if (session.isLastMessageHadKeyboard() && session.getLastKeyboardMessageId() > 0) {
                            removeInlineKeyboard(chatId, session.getLastKeyboardMessageId());
                        }

                        if (state.getInlineKeyboardSupplier().apply(session) == null) {
                            session.resetLastMessageKeyboardInfo();
                        } else {
                            int messageId = sentMessage.getMessageId();
                            session.setLastMessageKeyboardInfo(messageId);
                        }
                    } catch (Exception e) {
                        // fallback to silent if sending fails
                        silent.send(ERROR_MESSAGE, chatId);
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
            log.error("Failed to remove inline keyboard");
        }
    }
}