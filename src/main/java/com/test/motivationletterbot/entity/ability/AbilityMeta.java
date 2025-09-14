package com.test.motivationletterbot.entity.ability;

import com.test.motivationletterbot.entity.commands.CommandsEnum;

public record AbilityMeta(CommandsEnum commandEnum) {
    public String name() {
        return commandEnum.getBotCommand().getCommand().substring(1);
    }

    public String info() {
        return commandEnum.getBotCommand().getDescription();
    }
}
