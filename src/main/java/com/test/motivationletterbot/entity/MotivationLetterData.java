package com.test.motivationletterbot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Immutable
@Getter
@RequiredArgsConstructor
@Table(name = "motivation_letters_data")
public class MotivationLetterData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_description", nullable = false, columnDefinition = "TEXT")
    private final String roleDescription;

    @Column(name = "motivation", columnDefinition = "TEXT")
    private final String motivation;

    @Column(name = "generated_text", nullable = false, columnDefinition = "TEXT")
    private final String generatedText;

    @Column(name = "created_at", nullable = false)
    private final LocalDateTime createdAt;

    // Protected no-arg constructor for JPA
    protected MotivationLetterData() {
        this.roleDescription = null;
        this.motivation = null;
        this.generatedText = null;
        this.createdAt = null;
    }
}