package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;
import com.test.motivationletterbot.entity.UserSession;
import com.test.motivationletterbot.kafka.KafkaProducer;
import com.test.motivationletterbot.kafka.KafkaRequest;
import org.springframework.kafka.support.SendResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.motivationletterbot.MessageConstants.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
@Component
public class MotivationLetterBot extends AbilityBot implements SpringLongPollingBot {
    private final BotProperties botProperties;
    private final KafkaProducer kafkaProducer;
    private final ConcurrentHashMap<Long, UserSession> userSessions = new ConcurrentHashMap<>();
    private final long creatorId;

    public MotivationLetterBot(
            BotProperties botProperties,
            KafkaProducer kafkaProducer, TelegramClient telegramClient) {
        super(telegramClient, botProperties.getName());
        this.botProperties = botProperties;
        this.kafkaProducer = kafkaProducer;
        this.creatorId = botProperties.getBotCreatorId();
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

    private SendMessage buildSendMessage(long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }

    public void sendMessage(long chatId, String messageText) {
        SendMessage message = buildSendMessage(chatId, messageText);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void consume(Update update) {
        var message = update.getMessage();
        if (message != null) {
            UserSession session = userSessions.computeIfAbsent(message.getChatId(), id -> new UserSession());

            if (session.isVacancyOnWork() && message.hasText()) {
                log.warn("Vacancy is on work. Appending text: {}", message.getText());
                session.appendVacancy(message.getText());
            }

            super.consume(update);

            log.warn("Current vacancy text: " + session.getVacancy().toString());
        }
    }

    public Ability startRoleDescriptionWriting() {
        return Ability.builder()
                .name("start_rd")
                .info("Start role description writing")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    session.resetVacancy();
                    silent.send("Please provide your role description!", ctx.chatId());
                })
                .build();
    }

    public Ability endRoleDescriptionWriting() {
        return Ability.builder()
                .name("end_rd")
                .info("End role description writing")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    session.completeVacancy();
                    silent.send("Your role description was successfully recorded", ctx.chatId());
                })
                .build();
    }

    void sendToKafka(long chatId, String messageText) {
        CompletableFuture<SendResult<String, KafkaRequest>> future = kafkaProducer.sendRequest(
                new KafkaRequest(chatId, messageText)
        );
        future.whenComplete((result, e) -> {
            if (e != null) {
                log.error("Failed to send Kafka request", e);
                sendMessage(chatId, ERROR_MESSAGE);
            }
        });
    }

    @PostConstruct
    public void init() {
        this.onRegister();
        List<BotCommand> commands = getAbilities().values().stream()
                .filter(ability -> ability.info() != null && !ability.info().isBlank())
                .map(ability -> new BotCommand("/" + ability.name(), ability.info()))
                .toList();
        SetMyCommands setMyCommands = SetMyCommands.builder()
                .commands(commands)
                .build();
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands", e);
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.warn("Registered bot '{}' (token: {}) running state is: {}", botProperties.getName(), botProperties.getToken(), botSession.isRunning());
    }
}
