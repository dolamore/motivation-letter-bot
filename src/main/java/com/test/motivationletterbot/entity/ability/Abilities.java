package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.commands.CommandService;
import com.test.motivationletterbot.entity.UserSession;
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


import static com.test.motivationletterbot.constants.MessageConstants.ERROR_MESSAGE;
import static com.test.motivationletterbot.entity.ability.AbilitiesEnum.*;
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

    public Ability returnToMainMenu() {
        return getAbility(MENU_ABILITY).get();
    }


    public Ability writeMotivation() {
        return getAbility(WRITE_MOTIVATION_ABILITY).get();
    }

    public Ability continueMotivationWriting() {
        return getAbility(CONTINUE_MOTIVATION_ABILITY).get();
    }

    public Ability recordMotivation() {
        return getAbility(RECORD_MOTIVATION_ABILITY).get();
    }


    public Ability writeRoleDescription() {
        return getAbility(WRITE_ROLE_DESCRIPTION_ABILITY).get();
    }

    public Ability continueRoleDescriptionWriting() {
        return getAbility(CONTINUE_ROLE_DESCRIPTION_ABILITY).get();
    }

    public Ability recordRoleDescription() {
        return getAbility(RECORD_ROLE_DESCRIPTION_ABILITY).get();
    }


    public Ability writeAdditionalInformation() {
        return getAbility(WRITE_ADDITIONAL_INFORMATION_ABILITY).get();
    }

    public Ability continueAdditionalInformationWriting() {
        return getAbility(CONTINUE_ADDITIONAL_INFORMATION_ABILITY).get();
    }

    public Ability recordAdditionalInformation() {
        return getAbility(RECORD_ADDITIONAL_INFORMATION_ABILITY).get();
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
                    log.warn("User session: {}", session.getBotCommands().toString());
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