package com.example.playdemo.service;

import com.example.playdemo.config.ConfigProperties;
import com.example.playdemo.dto.SmsRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Implementacja interfejsu ExternalSmsSender do wysyłania wiadomości SMS.
 */
@Service
public class ExternalSmsSenderImpl implements ExternalSmsSender {
    private final RestTemplate restTemplate;
    private final ConfigProperties config;

    public ExternalSmsSenderImpl(RestTemplate restTemplate,
                                 ConfigProperties config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * Wysyła wiadomość SMS przez zewnętrzny serwis.
     * @param smsRequest Obiekt zawierający numer nadawcy, odbiorcy oraz treść wiadomości.
     */
    @Override
    public void sendSms(SmsRequest smsRequest) {
        String apiUrl = config.getExternalSmsUrl();

        // Tworzenie nagłówków HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SmsRequest> requestEntity = new HttpEntity<>(smsRequest, headers);

        System.out.println(smsRequest);
        // Wysłanie zapytania HTTP do API SMS
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // Logowanie statusu odpowiedzi
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("SMS wysłany pomyślnie: " + smsRequest.getRecipient());
        } else {
            System.err.println("Błąd podczas wysyłania SMS: " + response.getStatusCode());
        }
    }
}