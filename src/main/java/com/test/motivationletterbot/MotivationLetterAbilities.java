package com.test.motivationletterbot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class MotivationLetterAbilities {
    public Ability sayHelloWorld(SilentSender silent) {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .build();
    }

    public Ability saysHelloWorldToFriend(SilentSender silent) {
        return Ability.builder()
                .name("sayhi")
                .info("Says hi")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(1)
                .action(ctx -> silent.send("Hi " + ctx.firstArg(), ctx.chatId()))
                .build();
    }
}
