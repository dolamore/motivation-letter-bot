package com.test.motivationletterbot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextEntry {
    protected final StringBuffer text = new StringBuffer();
    protected String finalText;
    protected boolean onWork = false;
    protected boolean complete = false;

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

    public void complete(int lengthToRemove) {
        var len = text.length();
        text.delete(len - lengthToRemove, len);

        finalText = text.toString();

        complete = true;
        onWork = false;
    }

    public boolean isTextEmpty() {
        return text.length() == 0;
    }
}

