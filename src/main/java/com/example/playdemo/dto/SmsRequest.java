package com.example.playdemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SmsRequest {

    @NotBlank(message = "Numer nadawcy nie może być pusty")
    private String sender;

    @NotBlank(message = "Numer odbiorcy nie może być pusty")
    private String recipient;

    @NotBlank(message = "Treść wiadomości nie może być pusta")
    private String message;

    // Konstruktor z parametrami
    public SmsRequest(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }
}