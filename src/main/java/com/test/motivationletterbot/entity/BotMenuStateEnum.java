package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;

import static com.test.motivationletterbot.entity.CommandsEnum.*;

@Getter
@AllArgsConstructor
public enum BotMenuStateEnum {
    MAIN(EnumSet.of(RESTART_COMMAND, START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND)),
    MOTIVATION(EnumSet.of(START_COMMAND, START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND)),
    VACANCY(EnumSet.of(START_COMMAND, START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND)),
    SETTINGS(EnumSet.of(START_COMMAND, START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND));

    private final EnumSet<CommandsEnum> stateCommands;
}

