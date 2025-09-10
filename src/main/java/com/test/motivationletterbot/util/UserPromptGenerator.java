package com.test.motivationletterbot.util;

import com.test.motivationletterbot.entity.textentry.TextEntry;
import com.test.motivationletterbot.entity.textentry.TextEntryType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.test.motivationletterbot.constants.PromptConstants.USER_PROMPT_PATH;

public class UserPromptGenerator {

    private UserPromptGenerator() {
    }

    public static String buildUserPrompt(EnumMap<TextEntryType, TextEntry> entries) throws Exception {
        String userPrompt = Files.readString(Paths.get(USER_PROMPT_PATH));

        for (var entry : entries.entrySet()) {
            String key = entry.getKey().toString();
            String value = String.valueOf(entry.getValue().getText());
            userPrompt = userPrompt.replace("{{" + key + "}}", value);
        }

        return userPrompt;
    }
}
