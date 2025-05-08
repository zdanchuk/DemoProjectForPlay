package com.example.playdemo.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "sms_templates")
@Getter
public class SmsTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String templateKey;
    private String message;
}
