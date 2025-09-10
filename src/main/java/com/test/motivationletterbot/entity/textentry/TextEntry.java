package com.test.motivationletterbot.entity.textentry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.motivationletterbot.entity.commands.CommandsEnum;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.EnumSet;

@Getter
@Setter
public class TextEntry {
    private final StringBuffer text = new StringBuffer();
    private final boolean isMandatory;
    private String finalText = "";
    private boolean onWork = false;
    private boolean complete = false;
    private final TextEntryType type;


    @JsonCreator
    public TextEntry(@JsonProperty("type") TextEntryType type,
                     @JsonProperty("finalText") String finalText) {
        this.type = type;
        this.isMandatory = type.isMandatory();
        this.finalText = finalText != null ? finalText : "";
    }


    public TextEntry(TextEntryType type) {
        this.type = type;
        this.isMandatory = type.isMandatory();
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

    public boolean isBufferEmpty() {
        return text.isEmpty();
    }

    public void addButtonIfNotCompleted(EnumSet<CommandsEnum> commands) {
        if (!isComplete()) {
            commands.add(type.getMainMenuCommand());
        } else {
            commands.add(type.getRewriteCommand());
        }
    }

    public String getMenuMessage() {
        return type.getMenuMessage();
    }

    public String getWriteMessage() {
        if (isComplete()) {
            return type.getWriteCompletedMessage();
        } else {
            return type.getWriteMessage();
        }
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
}
