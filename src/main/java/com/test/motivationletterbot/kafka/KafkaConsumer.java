package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.bot.MotivationLetterBot;
import com.test.motivationletterbot.llm.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.test.motivationletterbot.util.UserPromptGenerator.buildUserPrompt;

@Slf4j
@Service
public class KafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, KafkaResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final LlmClient llmClient;

    public KafkaConsumer(
            @Lazy MotivationLetterBot motivationLetterBot,
            KafkaTemplate<String, KafkaResponse> responseKafkaTemplate,
            @Value("${motivation-bot.kafka.response-topic}") String responseTopic,
            @Qualifier("openAiClientService") LlmClient llmClient) {
        this.motivationLetterBot = motivationLetterBot;
        this.responseKafkaTemplate = responseKafkaTemplate;
        this.responseTopic = responseTopic;
        this.llmClient = llmClient;
    }

    @KafkaListener(topics = "${motivation-bot.kafka.request-topic}", groupId = "motivation-letter-bot")
    public void handleRequest(KafkaRequest request) throws Exception {
        String userPrompt = buildUserPrompt(request.getEntries());

        String generatedReply = llmClient.sendPrompt(userPrompt);
        log.warn("Generated text for chat ID {}: {}", request.getChatId(), generatedReply);
        KafkaResponse response = new KafkaResponse(request.getChatId(), generatedReply);
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(KafkaResponse response) {
        motivationLetterBot.sendMessage(response.getChatId(), response.getGeneratedText());
    }
}
