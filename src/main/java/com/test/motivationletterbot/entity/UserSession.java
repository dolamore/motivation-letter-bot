package com.test.motivationletterbot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {

    private boolean messageOnWork = false;
    private boolean sessionStarted = false;


    private final StringBuffer motivation = new StringBuffer();
    private String finalMotivation;
    private boolean isMotivationOnWork = false;
    private boolean isMotivationComplete = false;

    private final StringBuffer vacancy = new StringBuffer();
    private String finalVacancy;
    private boolean isVacancyOnWork = false;
    private boolean isVacancyComplete = false;

    private int lastKeyboardMessageId;
    private boolean lastMessageHadKeyboard = false;

    public void appendMotivation(String text) {
        if (text != null && !text.isEmpty()) {
            motivation.append(text);
        }
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
        sessionStarted = true;
    }

    public void endSession() {
        sessionStarted = false;
        startMotivationWriting();
        resetVacancy();
    }

    public void startMotivationWriting() {
        motivation.setLength(0);
        isMotivationComplete = false; //do we really need it?
        isMotivationOnWork = true;
    }

    public void appendVacancy(String text) {
        if (text != null && !text.isEmpty()) {
            vacancy.append(text);
        }
    }

    public void resetVacancy() {
        vacancy.setLength(0);
        isVacancyComplete = false;
        isVacancyOnWork = true;
    }

    public void completeVacancy() {
        // Remove the last 7 characters ("/end_rd") from the buffer
        var len = vacancy.length();
        vacancy.delete(len - 7, len);

        finalVacancy = vacancy.toString();

        isVacancyComplete = true;
        isVacancyOnWork = false;
    }

    public void vacancyOnWork() {
        this.isVacancyOnWork = true;
    }

    public void motivationOnWork() {
        this.isMotivationOnWork = true;
    }

    public void completeMotivation() {
        // Remove the last 6 characters ("/end_m") from the buffer
        var len = motivation.length();
        motivation.delete(len - 6, len);

        finalMotivation = motivation.toString();

        isMotivationComplete = true;
        isMotivationOnWork = false;
    }

    public String returnMessage() {
        return "message";
    }
}