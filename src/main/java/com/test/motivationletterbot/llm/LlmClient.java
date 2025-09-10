package com.test.motivationletterbot.llm;


public interface LlmClient {
    String sendPrompt(String payload) throws Exception;
}
