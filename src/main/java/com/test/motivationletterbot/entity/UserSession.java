package com.test.motivationletterbot.entity;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.test.motivationletterbot.MessageConstants.STARTING_MESSAGE;

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