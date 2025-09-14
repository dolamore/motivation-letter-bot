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
public class KafkaRequest {
    private Long chatId;
    private String text;
    private AbilitiesEnum state;
}
