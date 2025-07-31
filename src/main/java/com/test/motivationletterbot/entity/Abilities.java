package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;

import java.util.concurrent.ConcurrentHashMap;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.ALL;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

@AllArgsConstructor
public class Abilities {
    private final ConcurrentHashMap<Long, UserSession> userSessions;
    private final SilentSender silent;

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
}
