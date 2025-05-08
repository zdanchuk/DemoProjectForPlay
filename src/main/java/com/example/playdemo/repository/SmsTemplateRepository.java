package com.example.playdemo.repository;

import com.example.playdemo.model.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    /**
     * Pobiera szablon wiadomości na podstawie klucza.
     */
    SmsTemplate findByTemplateKey(String templateKey);
}
