package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;

import static com.test.motivationletterbot.entity.CommandsEnum.*;

@Getter
@AllArgsConstructor
public enum BotMenuStateEnum {
    START_MENU_STATE(EnumSet.of(START_COMMAND)),
    MAIN_MENU_STATE(EnumSet.of(RESTART_COMMAND, WRITE_MOTIVATION_COMMAND, WRITE_ROLE_DESCRIPTION_COMMAND)),
    MOTIVATION_MENU_STATE(EnumSet.of(START_COMMAND, WRITE_MOTIVATION_COMMAND, WRITE_ROLE_DESCRIPTION_COMMAND)),
    VACANCY_MENU_STATE(EnumSet.of(START_COMMAND, WRITE_MOTIVATION_COMMAND, WRITE_ROLE_DESCRIPTION_COMMAND));

    private final EnumSet<CommandsEnum> stateCommands;
}

