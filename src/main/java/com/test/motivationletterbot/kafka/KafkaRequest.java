package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.entity.textentry.TextEntry;
import com.test.motivationletterbot.entity.textentry.TextEntryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaRequest {
    private Long chatId;
    private EnumMap<TextEntryType, TextEntry> entries;
}
