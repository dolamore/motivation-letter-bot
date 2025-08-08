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
    CONTINUE_ROLE_DESCRIPTION_COMMAND("/cont_rd", "Continue writing role description"),
    SUBMIT_ROLE_DESCRIPTION_COMMAND("/submit_rd", "Submit role description"),

    WRITE_MOTIVATION_COMMAND("/write_m", "Write motivation"),
    REWRITE_MOTIVATION_COMMAND("/write_m", "Rewrite motivation"),
    CONTINUE_MOTIVATION_COMMAND("/cont_m", "Continue writing motivation"),
    SUBMIT_MOTIVATION_COMMAND("/submit_m", "Submit motivation"),

    WRITE_ADDITIONAL_INFORMATION_COMMAND("/write_ai", "Write additional information"),
    REWRITE_ADDITIONAL_INFORMATION_COMMAND("/write_ai", "Rewrite additional information"),
    CONTINUE_ADDITIONAL_INFORMATION_COMMAND("/cont_ai", "Continue writing additional information"),
    SUBMIT_ADDITIONAL_INFORMATION_COMMAND("/submit_ai", "Submit additional information");


    private final BotCommand botCommand;

    CommandsEnum(String command, String description) {
        this.botCommand = new BotCommand(command, description);
    }
}
