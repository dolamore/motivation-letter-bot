package com.test.motivationletterbot.llm;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Getter
public class SystemPromptProvider {

    private final String systemPrompt;

    public SystemPromptProvider(@Value("${llm.system-prompt-path}") Resource systemPromptResource) {
        String loaded;

        try (InputStream in = systemPromptResource.getInputStream()) {
            loaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Warning: failed to load system prompt from '" + systemPromptResource + "': " + e.getMessage());
            loaded = "";
        }

        this.systemPrompt = loaded;
    }
}