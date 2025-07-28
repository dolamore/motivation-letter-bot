package com.test.motivationletterbot.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Service
public class MotivationLetterKafkaProducer {
    private final KafkaTemplate<String, MotivationLetterRequest> kafkaTemplate;
    private final String requestTopic;

    public MotivationLetterKafkaProducer(
            KafkaTemplate<String, MotivationLetterRequest> kafkaTemplate,
            @Value("${motivation-bot.kafka.request-topic}") String requestTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestTopic = requestTopic;
    }

    public CompletableFuture<SendResult<String, MotivationLetterRequest>> sendRequest(MotivationLetterRequest request) {
        return kafkaTemplate.send(requestTopic, request).toCompletableFuture();
    }
}
