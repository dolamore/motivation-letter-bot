package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.entity.UserSession;
import com.test.motivationletterbot.entity.ability.AbilitiesEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaResponse {
    private Long chatId;
    private String generatedText;
    private UserSession session;
    private AbilitiesEnum state;
}
