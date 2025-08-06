package com.test.motivationletterbot.entity;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


@Getter
public enum CommandsEnum {
    START_COMMAND("/start", "Start writing message"),
    RESTART_COMMAND("/new", "New motivation message"),
    MENU_COMMAND("/menu", "Return to the main menu"),

    START_MOTIVATION_COMMAND("/start_m", "Write motivation"),
    SUBMIT_MOTIVATION_COMMAND("/submit_m", "Submit motivation"),
    DROP_MOTIVATION_COMMAND("/drop_m", "Drop motivation"),

    START_ROLE_DESCRIPTION_COMMAND("/start_rd", "Write role description"),
    SUBMIT_ROLE_DESCRIPTION_COMMAND("/submit_rd", "Submit role description"),
    DROP_ROLE_DESCRIPTION_COMMAND("/drop_rd", "Drop role description");

    private final BotCommand botCommand;

    CommandsEnum(String command, String description) {
        this.botCommand = new BotCommand(command, description);
    }
}
