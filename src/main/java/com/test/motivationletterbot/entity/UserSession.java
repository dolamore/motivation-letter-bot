package com.test.motivationletterbot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private final StringBuffer motivation = new StringBuffer();
    private final StringBuffer vacancy = new StringBuffer();
    private boolean vacancyOnWork = false;
    private boolean motivationOnWork = false;
    private boolean motivationIsComplete = false;
    private boolean vacancyIsComplete = false;

    public void appendMotivation(String text) {
        if (text != null && !text.isEmpty()) {
            motivation.append(text);
        }
    }

    public void resetMotivation() {
        motivation.setLength(0);
        motivationIsComplete = false;
        motivationOnWork = false;
    }

    public void appendVacancy(String text) {
        if (text != null && !text.isEmpty()) {
            vacancy.append(text);
        }
    }

    public void resetVacancy() {
        vacancy.setLength(0);
        vacancyIsComplete = false;
        vacancyOnWork = false;
    }

    public void vacancyIsComplete() {
        this.vacancyIsComplete = true;
    }

    public void vacancyOnWork() {
        this.vacancyOnWork = true;
    }

    public void motivationOnWork() {
        this.motivationOnWork = true;
    }

    public void motivationIsComplete() {
        this.motivationIsComplete = true;
    }
}
