package com.test.motivationletterbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivationLetterDataRepository extends JpaRepository<MotivationLetterData, Long> {
    // You can add custom query methods here if needed
}

