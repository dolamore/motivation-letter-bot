package com.test.motivationletterbot;

import org.springframework.stereotype.Service;

@Service
public class MotivationLetterService {

    public String buildResponseText(String messageText) {
        return "You said: " + messageText;
    }
}
