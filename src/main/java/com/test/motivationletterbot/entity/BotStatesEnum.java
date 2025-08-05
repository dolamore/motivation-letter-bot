//package com.test.motivationletterbot.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
//
//import java.util.List;
//
//import static com.test.motivationletterbot.MessageConstants.*;
//import static com.test.motivationletterbot.entity.AbilitiesEnum.*;
//
//@Getter
//@AllArgsConstructor
//public enum BotStatesEnum {
//    START_STATE(
//            START_ABILITY,
//            STARTING_MESSAGE
//    ),
//    MENU_STATE(
//            MENU_ABILITY,
//            STARTING_MESSAGE
//    ),
//    MOTIVATION_STATE(
//            START_MOTIVATION_ABILITY,
//            STARTING_MESSAGE
//    ),
//    ROLE_DESCRIPTION_STATE(
//            START_ROLE_DESCRIPTION_ABILITY,
//            STARTING_MESSAGE
//    );
//
//
//    private final AbilitiesEnum ability;
//    private final String message;
//
//    public List<BotCommand> getCommands() {
//        return ability.getCommands();
//    }
//}
