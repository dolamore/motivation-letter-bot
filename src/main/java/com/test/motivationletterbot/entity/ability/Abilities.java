package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.commands.CommandService;
import com.test.motivationletterbot.entity.UserSession;
import com.test.motivationletterbot.kafka.KafkaProducer;
import com.test.motivationletterbot.kafka.KafkaRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private final KafkaProducer kafkaProducer;

    public Abilities(ConcurrentHashMap<Long, UserSession> userSessions, SilentSender silent, TelegramClient telegramClient, CommandService commandService, KafkaProducer kafkaProducer) {
        this.commandService = commandService;
        this.userSessions = userSessions;
        this.silent = silent;
        this.telegramClient = telegramClient;
        this.kafkaProducer = kafkaProducer;
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

    public Ability writeRoleDescription() {
        return getAbility(WRITE_ROLE_DESCRIPTION_ABILITY).get();
    }

    public Ability writeAdditionalInformation() {
        return getAbility(WRITE_ADDITIONAL_INFORMATION_ABILITY).get();
    }


    public Ability recordTextEntry() {
        return getAbility(RECORD_TEXT_ENTRY_ABILITY).get();
    }

    public Ability continueWriting() {
        return getAbility(CONTINUE_ABILITY).get();
    }

    public Ability generateMessage() {
        return getGenerativeAbility().get();
    }


    private Supplier<Ability> getAbility(AbilitiesEnum state) {
        return () -> Ability.builder().name(state.getAbilityName()).info(state.getInfo()).privacy(PUBLIC).locality(ALL).action(ctx -> {
            long chatId = ctx.chatId();
            UserSession session = prepareSessionAndCommands(chatId, state);
            SendMessage sendMessage = buildSendMessage(chatId, session, state);
            sendAndHandleKeyboard(chatId, session, sendMessage, state);
        }).build();
    }

    private Supplier<Ability> getGenerativeAbility() {
        return () -> Ability.builder().name("generate").info("Generate a message").privacy(PUBLIC).locality(ALL).action(ctx -> {
            long chatId = ctx.chatId();
            UserSession session = prepareSessionAndCommands(chatId, GENERATE_ABILITY);
            sendToKafkaAndHandleKeyboard(chatId, session);
        }).build();
    }

    private UserSession prepareSessionAndCommands(long chatId, AbilitiesEnum state) {
        UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
        state.getSessionAction().accept(session);
        commandService.setBotCommands(chatId, session.getBotCommands());
        return session;
    }

    private SendMessage buildSendMessage(long chatId, UserSession session, AbilitiesEnum state) {
        return SendMessage.builder().chatId(chatId).text(state.getMessage().apply(session)).replyMarkup(buildReplyMarkup(session, state)).build();
    }


    private InlineKeyboardMarkup buildReplyMarkup(UserSession session, AbilitiesEnum state) {
        List<InlineKeyboardRow> rows = Optional.ofNullable(state.getInlineKeyboardSupplier())
                .map(supplier -> supplier.apply(session))
                .filter(Objects::nonNull)
                .orElse(Collections.emptyList());

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
        return markup;
    }

    private void sendAndHandleKeyboard(long chatId, UserSession session, SendMessage sendMessage, AbilitiesEnum state) {
        try {
            Message sentMessage = telegramClient.execute(sendMessage);

            removePreviousInlineKeyboardIfPresent(chatId, session);

            if (state.getInlineKeyboardSupplier().apply(session) == null) {
                session.resetLastMessageKeyboardInfo();
            } else {
                int messageId = sentMessage.getMessageId();
                session.setLastMessageKeyboardInfo(messageId);
            }
        } catch (Exception e) {
            silent.send(ERROR_MESSAGE, chatId);
        }
    }

    private void sendToKafkaAndHandleKeyboard(long chatId, UserSession session) {
        removePreviousInlineKeyboardIfPresent(chatId, session);

        String userPrompt = session.generatedText();

        sendToKafka(chatId, userPrompt, session);

    }

    private void removeInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder().chatId(chatId).messageId(messageId).replyMarkup(null).build();
        try {
            try {
                telegramClient.execute(editMarkup);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("Failed to remove inline keyboard");
        }
    }

    private void removePreviousInlineKeyboardIfPresent(long chatId, UserSession session) {
        if (session.isLastMessageHadKeyboard() && session.getLastKeyboardMessageId() > 0) {
            removeInlineKeyboard(chatId, session.getLastKeyboardMessageId());
        }
    }

    private void sendToKafka(long chatId, String userPrompt, UserSession session) {
        CompletableFuture<SendResult<String, KafkaRequest>> future = kafkaProducer.sendRequest(new KafkaRequest(chatId, userPrompt, GENERATE_ABILITY));
        future.whenComplete((result, e) -> {
            if (e != null) {
                log.error("Failed to send Kafka request", e);
                silent.send(ERROR_MESSAGE, chatId);
            } else {
                silent.send("Generating...", chatId);
            }
        });
    }
}