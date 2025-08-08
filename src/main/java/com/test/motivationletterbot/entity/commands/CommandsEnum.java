package com.test.motivationletterbot.entity.commands;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


@Getter
public enum CommandsEnum {
    START_COMMAND("/start", "Start writing message"),
    RESTART_COMMAND("/new", "New motivation message"),
    MENU_COMMAND("/menu", "Return to the main menu"),

    WRITE_ROLE_DESCRIPTION_COMMAND("/write_rd", "Write role description"),
    REWRITE_ROLE_DESCRIPTION_COMMAND("/write_rd", "Rewrite role description"),

    WRITE_MOTIVATION_COMMAND("/write_m", "Write motivation"),
    REWRITE_MOTIVATION_COMMAND("/write_m", "Rewrite motivation"),

    WRITE_ADDITIONAL_INFORMATION_COMMAND("/write_ai", "Write additional information"),
    REWRITE_ADDITIONAL_INFORMATION_COMMAND("/write_ai", "Rewrite additional information"),

    CONTINUE_COMMAND("/cont", "Continue writing"),
    SUBMIT_COMMAND("/submit", "Submit");


    private final BotCommand botCommand;

    CommandsEnum(String command, String description) {
        this.botCommand = new BotCommand(command, description);
    }
}
