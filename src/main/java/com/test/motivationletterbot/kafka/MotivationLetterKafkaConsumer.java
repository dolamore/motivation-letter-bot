package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.MotivationLetterBot;
import com.test.motivationletterbot.MotivationLetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MotivationLetterKafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, MotivationLetterResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final MotivationLetterService motivationLetterService;

    public MotivationLetterKafkaConsumer(
            MotivationLetterBot motivationLetterBot,
            KafkaTemplate<String, MotivationLetterResponse> responseKafkaTemplate,
            @Value("${motivation-bot.kafka.response-topic}") String responseTopic,
            MotivationLetterService motivationLetterService) {
        this.motivationLetterBot = motivationLetterBot;
        this.responseKafkaTemplate = responseKafkaTemplate;
        this.responseTopic = responseTopic;
        this.motivationLetterService = motivationLetterService;
    }

    @KafkaListener(topics = "${motivation-bot.kafka.request-topic}", groupId = "motivation-letter-bot")
    public void handleRequest(MotivationLetterRequest request) {
        String generatedText = motivationLetterService.buildResponseText(request.getMessageText());
        log.warn("Generated text for chat ID {}: {}", request.getChatId(), generatedText);
        MotivationLetterResponse response = new MotivationLetterResponse(request.getChatId(), generatedText);
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(MotivationLetterResponse response) {
        motivationLetterBot.sendMessage(response.getChatId(), response.getGeneratedText());
    }
}
