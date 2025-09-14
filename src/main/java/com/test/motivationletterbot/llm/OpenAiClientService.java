package com.test.motivationletterbot.llm;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public final class OpenAiClientService extends AbstractLlmClient {
    private final OpenAIClient client;
    private final ChatModel model;

    public OpenAiClientService(
            @Value("${openai.api.key:${OPENAI_API_KEY:}}") String apiKey,
            SystemPromptProvider systemPromptProvider
    ) {
        super(systemPromptProvider);
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        ;
        this.model = ChatModel.GPT_5;
    }

    public String sendPrompt(String userPrompt) {
        log.warn("Sending prompt to OpenAI: {}", userPrompt);
        log.warn("System prompt: {}", getSystemPrompt());

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(getSystemPrompt())
                .addUserMessage(userPrompt)
                .build();

        ChatCompletion chat = client.chat().completions().create(params);

        return chat.choices().getFirst().message().content().orElse("No response");
    }
}



