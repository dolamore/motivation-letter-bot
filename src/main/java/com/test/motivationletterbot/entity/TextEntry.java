package com.test.motivationletterbot.entity;

import com.test.motivationletterbot.entity.commands.CommandsEnum;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumSet;

@Getter
@Setter
public class TextEntry {
    private final StringBuffer text = new StringBuffer();
    private String finalText;
    private boolean onWork = false;
    private boolean complete = false;
    private final TextEntryType type;

    public TextEntry(TextEntryType type) {
        this.type = type;
    }

    public void reset() {
        getText().setLength(0);
        setFinalText("");
        setOnWork(false);
        setComplete(false);
    }

    public void startWriting() {
        getText().setLength(0);
        setOnWork(true);
    }

    public void append(String text) {
        if (text != null && !text.isEmpty()) {
            getText().append(text);
        }
    }

    public void complete() {
        finalText = text.toString();

        complete = true;
        onWork = false;
    }

    public void addButtonIfNotCompleted(EnumSet<CommandsEnum> commands) {
        if (!isComplete()) {
            commands.add(type.getMainMenuCommand());
        }
    }

    public String getMenuMessage() {
        return type.getMenuMessage();
    }

    public String getWriteMessage() {
        return type.getWriteMessage();
    }

    public String getContinueMessage() {
        if (isComplete()) {
            return type.getContinueCompletedMessage();
        }
        return type.getContinueMessage();
    }

    public InlineKeyboardRow getKeyboardRow() {
        return type.getKeyboardRow();
    }

    public InlineKeyboardRow getSubmitKeyboardRow() {
        return type.getSubmitKeyboardRow();
    }

    public CommandsEnum getSubmitCommand() {
        return type.getSubmitCommand();
    }
}
