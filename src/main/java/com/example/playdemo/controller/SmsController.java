package com.example.playdemo.controller;

import com.example.playdemo.dto.SmsRequest;
import com.example.playdemo.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler REST API do obsługi wiadomości SMS.
 */
@RestController
@RequestMapping("/sms")
public class SmsController {
    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Metoda obsługująca odbieranie wiadomości SMS.
     * Wywołuje logikę przetwarzania wiadomości w SmsService.
     *
     * @param smsRequest Obiekt zawierający dane nadawcy, odbiorcy i treść wiadomości.
     * @return Odpowiedź HTTP informująca o przetworzeniu wiadomości.
     */
    @PostMapping("/receive")
    public ResponseEntity<String> receiveSms(@RequestBody SmsRequest smsRequest) {
        smsService.processSms(smsRequest);
        return ResponseEntity.ok("Wiadomość została przetworzona");
    }
}