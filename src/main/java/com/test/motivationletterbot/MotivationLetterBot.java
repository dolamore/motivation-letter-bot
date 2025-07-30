package com.test.motivationletterbot;

import com.test.motivationletterbot.entity.BotProperties;
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
    private final ConcurrentHashMap<Long, StringBuilder> motivation = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, StringBuilder> vacancy = new ConcurrentHashMap<>();
    private boolean messageIsComplete = false;
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
        super.consume(update);
        var message = update.getMessage();
        if (message != null && message.hasText()) {
            String messageText = message.getText();
            long chat_id = message.getChatId();


            if (messageIsComplete) {
                sendMessage(chat_id, PROCESSING_MESSAGE);
                sendToKafka(chat_id, messageText);
            }
        }
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

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .build();
    }

    public Ability saysHelloWorldToFriend() {
        return Ability.builder()
                .name("sayhi")
                .info("says hi to a friend")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(1)
                .action(ctx -> silent.send("Hi " + ctx.firstArg(), ctx.chatId()))
                .build();
    }
}
