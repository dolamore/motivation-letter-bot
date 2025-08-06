package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import com.test.motivationletterbot.entity.keyboard.InlineKeyboards;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.test.motivationletterbot.constants.MessageConstants.STARTING_MESSAGE;
import static com.test.motivationletterbot.entity.commands.BotMenuStateEnum.*;

@Getter
@Setter
public class UserSession {
    @Setter
    private static InlineKeyboards inlineKeyboards;

    private boolean messageOnWork = false;
    private boolean sessionStarted = false;

    private final List<TextEntry> entries = new ArrayList<>();
    private final TextEntry motivation = new TextEntry();
    private final TextEntry vacancy = new TextEntry();

    private int lastKeyboardMessageId;
    private boolean lastMessageHadKeyboard = false;

    private EnumSet<CommandsEnum> menuState = START_MENU_STATE.getStateCommands();


    public List<BotCommand> getBotCommands() {
        List<BotCommand> commands = new ArrayList<>();
        for (CommandsEnum command : menuState) {
            commands.add(command.getBotCommand());
        }
        return commands;
    }

    public void resetLastMessageKeyboardInfo() {
        lastKeyboardMessageId = 0;
        lastMessageHadKeyboard = false;
    }

    public void setLastMessageKeyboardInfo(int messageId) {
        lastKeyboardMessageId = messageId;
        lastMessageHadKeyboard = true;
    }

    public void startSession() {
        motivation.reset();
        vacancy.reset();
    }

    public boolean isMotivationComplete() {
        return motivation.isComplete();
    }

    public boolean isVacancyComplete() {
        return vacancy.isComplete();
    }


    public void startMotivationWriting() {
        motivation.startWriting();
    }

    public void appendVacancy(String text) {
        vacancy.append(text);
    }

    public void resetVacancy() {
        vacancy.reset();
    }

    public void completeVacancy() {
        // Remove the last 7 characters ("/end_rd") from the buffer
        vacancy.complete(7);
    }

    public void completeMotivation() {
        // Remove the last 6 characters ("/end_m") from the buffer
        motivation.complete(6);
    }

    public String returnMessage() {
        return "message";
    }

    public String startingMessage() {
        return STARTING_MESSAGE;
    }

    public String restartingMessage() {
        return "You can start writing a new motivation letter or role description.";
    }

    public List<InlineKeyboardRow> startKeyboard() {
        return inlineKeyboards.startKeyboard();
    }
}