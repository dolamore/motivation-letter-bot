package com.test.motivationletterbot.entity;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


@Getter
public enum CommandsEnum {
    START_COMMAND("/start", "Start the bot and get a welcome message"),
    MENU_COMMAND("/menu", "Show the main menu"),
    START_MOTIVATION_COMMAND("/start_m", "Start writing a motivation message"),
    END_MOTIVATION_COMMAND("/end_m", "End writing a motivation message"),
    START_ROLE_DESCRIPTION_COMMAND("/start_rd", "Start writing a role description"),
    END_ROLE_DESCRIPTION_COMMAND("/end_rd", "End writing a role description");

    private final BotCommand botCommand;

    CommandsEnum(String command, String description) {
        this.botCommand = new BotCommand(command, description);
    }
}
