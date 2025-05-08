package com.example.playdemo.service;

import com.example.playdemo.dto.SmsRequest;

/**
 * Interfejs do wysyłania wiadomości SMS przez zewnętrzny serwis.
 */
public interface ExternalSmsSender {
    void sendSms(SmsRequest smsRequest);
}