package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.entity.ability.AbilitiesEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaResponse {
    private Long chatId;
    private String generatedText;
    private AbilitiesEnum state;
}
