package com.test.motivationletterbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;

import static com.test.motivationletterbot.entity.CommandsEnum.*;

@Getter
@AllArgsConstructor
public enum BotMenuStateEnum {
    MAIN(EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND)),
    MOTIVATION(EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND)),
    VACANCY(EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND)),
    SETTINGS(EnumSet.of(START_MOTIVATION_COMMAND, START_ROLE_DESCRIPTION_COMMAND, START_COMMAND));

    private EnumSet<CommandsEnum> stateCommands;
}

