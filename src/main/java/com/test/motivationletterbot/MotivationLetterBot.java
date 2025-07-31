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

            if (session.isMotivationOnWork() && message.hasText()) {
                log.warn("Motivation is on work. Appending text: {}", message.getText());
                session.appendMotivation(message.getText());
            }

            super.consume(update);

            log.warn("Current vacancy text: {}", session.getVacancy());
            log.warn("Current motivation text: {}", session.getMotivation());
        }
    }

    private Ability buildAbility(String name, String info, java.util.function.Consumer<UserSession> sessionAction, String message) {
        return Ability.builder()
                .name(name)
                .info(info)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    long chatId = ctx.chatId();
                    UserSession session = userSessions.computeIfAbsent(chatId, id -> new UserSession());
                    sessionAction.accept(session);
                    silent.send(message, chatId);
                })
                .build();
    }

    public Ability startMotivationWriting() {
        return buildAbility(
                "start_m",
                "Start motivation writing",
                UserSession::resetMotivation,
                "Please provide your motivation text!"
        );
    }

    public Ability endMotivationWriting() {
        return buildAbility(
                "end_m",
                "End motivation writing",
                UserSession::completeMotivation,
                "Your motivation text was successfully recorded"
        );
    }

    public Ability startRoleDescriptionWriting() {
        return buildAbility(
                "start_rd",
                "Start role description writing",
                UserSession::resetVacancy,
                "Please provide your role description!"
        );
    }

    public Ability endRoleDescriptionWriting() {
        return buildAbility(
                "end_rd",
                "End role description writing",
                UserSession::completeVacancy,
                "Your role description was successfully recorded"
        );
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
