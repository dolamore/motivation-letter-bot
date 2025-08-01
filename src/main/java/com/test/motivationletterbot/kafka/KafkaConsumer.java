package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.MotivationLetterBot;
import com.test.motivationletterbot.MotivationLetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, KafkaResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final MotivationLetterService motivationLetterService;

    public KafkaConsumer(
            @Lazy MotivationLetterBot motivationLetterBot,
            KafkaTemplate<String, KafkaResponse> responseKafkaTemplate,
            @Value("${motivation-bot.kafka.response-topic}") String responseTopic,
            MotivationLetterService motivationLetterService) {
        this.motivationLetterBot = motivationLetterBot;
        this.responseKafkaTemplate = responseKafkaTemplate;
        this.responseTopic = responseTopic;
        this.motivationLetterService = motivationLetterService;
    }

    @KafkaListener(topics = "${motivation-bot.kafka.request-topic}", groupId = "motivation-letter-bot")
    public void handleRequest(KafkaRequest request) {
        String generatedText = motivationLetterService.buildResponseText(request.getMessageText());
        log.warn("Generated text for chat ID {}: {}", request.getChatId(), generatedText);
        KafkaResponse response = new KafkaResponse(request.getChatId(), generatedText);
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(KafkaResponse response) {
        motivationLetterBot.sendMessage(response.getChatId(), response.getGeneratedText());
    }
}
