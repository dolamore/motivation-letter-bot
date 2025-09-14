package com.test.motivationletterbot.llm;

import org.springframework.stereotype.Service;

@Service
public abstract class AbstractLlmClient implements LlmClient {

    protected final String systemPrompt;

    public AbstractLlmClient(SystemPromptProvider provider) {
        this.systemPrompt = provider.getSystemPrompt();
    }

    protected String getSystemPrompt() {
        return systemPrompt;
    }
}
