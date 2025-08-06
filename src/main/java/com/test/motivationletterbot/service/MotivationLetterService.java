package com.test.motivationletterbot.service;

import org.springframework.stereotype.Service;

@Service
public class MotivationLetterService {

    public String buildResponseText(String messageText) {
        return "You said: " + messageText;
    }
}
