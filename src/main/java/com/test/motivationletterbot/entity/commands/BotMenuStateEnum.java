package com.test.motivationletterbot.entity.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;

import static com.test.motivationletterbot.entity.commands.CommandsEnum.*;

@Getter
@AllArgsConstructor
public enum BotMenuStateEnum {
    START_MENU_BASE_STATE(EnumSet.of(RESTART_COMMAND));

    private final EnumSet<CommandsEnum> stateCommands;
    }

