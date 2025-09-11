package com.test.motivationletterbot.bot;

import com.test.motivationletterbot.entity.*;
import com.test.motivationletterbot.entity.ability.Abilities;
import com.test.motivationletterbot.entity.ability.AbilitiesEnum;
import com.test.motivationletterbot.entity.commands.CommandService;
import com.test.motivationletterbot.kafka.KafkaProducer;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.motivationletterbot.util.BotUtils.isCommand;

import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;

import com.test.motivationletterbot.entity.textentry.TextEntryType;

@Slf4j
@Component
@Getter
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final long creatorId;
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final CommandService commandService;

    public MotivationLetterBot(
            BotProperties botProperties,
            TelegramClient telegramClient,
            CommandService commandService,
            BareboneToggle toggle,
            ConcurrentHashMap<Long, UserSession> userSessions,
            SilentSender silent,
            KafkaProducer kafkaProducer) {
        super(
                telegramClient,
                botProperties.getName(),
                useInMemoryMapDB(),
                toggle
        );
        addExtensions(new Abilities(userSessions, silent, telegramClient, commandService, kafkaProducer));
        this.botProperties = botProperties;
        this.creatorId = botProperties.getBotCreatorId();
        this.userSessions = userSessions;
        this.commandService = commandService;
    }

    @Override
    public void consume(Update update) {
        if (!checkGlobalFlags(update)) {
            log.warn("There is nothing i can do with this update");
            return;
        }

        UserSession session = getOrCreateUserSession(update);
        var ctx = buildContext(update);

        if (update.hasCallbackQuery()) {
            var call_data = update.getCallbackQuery().getData();

            getAbilities().get(call_data).action().accept(ctx);

            return;
        }

        super.consume(update);

        Optional.ofNullable(update.getMessage()).ifPresent(message -> {
            if (isCommand(message)) {
                return;
            }

            boolean found = false;
            for (var type : TextEntryType.values()) {
                if (session.isOnWork(type)) {
                    session.addText(type, message.getText());
                    getAbilities().get("cont").action().accept(ctx);
                    found = true;
                    break;
                }
            }

            if (!found) {
                getAbilities().get("menu").action().accept(ctx);
            }
        });

    }

    private static DBContext useInMemoryMapDB() {
        String useInMemory = System.getProperty("bot.use-inmemory-db",
                System.getenv("USE_INMEMORY_MAPDB"));

        String dbName;
        if ("true".equalsIgnoreCase(useInMemory)) {
            // Use a unique in-memory DB name for each run, stored in a temp folder
            dbName = "./tmpdb/inmem-" + UUID.randomUUID();
        } else {
            dbName = "./tmpdb/MotivationLetterBot-" + System.currentTimeMillis();

        }
        return MapDBContext.offlineInstance(dbName);
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public void sendPdf(long chatId, AbilitiesEnum state, File pdfFile) {
        var session = userSessions.get(chatId);
        SendDocument sendDocument = buildSendPdf(chatId, session, state, pdfFile);
        Message sentDocument = null;
        try {
            sentDocument = telegramClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Failed to send PDF document", e);
        }
        state.getSessionAction().accept(session);
        commandService.setBotCommands(chatId, session.getBotCommands());
        if (sentDocument != null) {
            if (state.getInlineKeyboardSupplier().apply(session) == null) {
                session.resetLastMessageKeyboardInfo();
            } else {
                int messageId = sentDocument.getMessageId();
                session.setLastMessageKeyboardInfo(messageId);
                log.warn("Updated session after sending PDF: {}", session);
            }
        }
    }

    private SendDocument buildSendPdf(long chatId, UserSession session, AbilitiesEnum state, File pdfFile) {
        return SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(pdfFile))
                .replyMarkup(buildReplyMarkup(session, state))
                .build();
    }

    private InlineKeyboardMarkup buildReplyMarkup(UserSession session, AbilitiesEnum state) {
        return InlineKeyboardMarkup.builder().keyboard(Optional.ofNullable(state.getInlineKeyboardSupplier()).map(supplier -> supplier.apply(session)).filter(Objects::nonNull).orElse(Collections.emptyList())).build();
    }

    private UserSession getOrCreateUserSession(Update update) {
        long chatId = update.getMessage() != null
                ? update.getMessage().getChatId()
                : update.getCallbackQuery().getMessage().getChatId();
        return userSessions.computeIfAbsent(chatId, id -> new UserSession());
    }

    private MessageContext buildContext(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            return MessageContext.newContext(update, callback.getFrom(), callback.getMessage().getChatId(), this);
        } else if (update.hasMessage()) {
            var message = update.getMessage();
            return MessageContext.newContext(update, message.getFrom(), message.getChatId(), this);
        }
        throw new IllegalArgumentException("Update does not contain a message or callback query");
    }

    @Override
    public boolean checkGlobalFlags(Update update) {
        return Flag.TEXT.test(update) || Flag.CALLBACK_QUERY.test(update);
    }

    @PostConstruct
    public void init() {
        this.onRegister();
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("\n\n\n{}: Registered bot '{}' (token: {}) running state is: {}", new Date(), botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }
}