package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.bot.MotivationLetterBot;
import com.test.motivationletterbot.llm.LlmClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Getter
public class KafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, KafkaResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final LlmClient llmClient;

    // cached font bytes to avoid repeated file reads; used to create PDType0Font per document
    private final AtomicReference<byte[]> cachedFontData = new AtomicReference<>();

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
        String generatedReply = llmClient.sendPrompt(request.getText());
        KafkaResponse response = new KafkaResponse(request.getChatId(), generatedReply, request.getState());
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(KafkaResponse response) {

        motivationLetterBot.sendGenerativeMessage(response.getChatId(), response.getGeneratedText());
    }
}
