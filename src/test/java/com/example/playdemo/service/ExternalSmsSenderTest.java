package com.example.playdemo.service;

import com.example.playdemo.config.ConfigProperties;
import com.example.playdemo.dto.SmsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalSmsSenderTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConfigProperties config;

    @InjectMocks
    private ExternalSmsSenderImpl externalSmsSender;

    // Przygotowanie danych
    private final SmsRequest smsRequest = new SmsRequest("SERVICE", "123456789", "Testowa wiadomość");
    private final String apiUrl = "https://external-sms-service.com/api/send";

    /**
     * Testuje poprawne wysyłanie wiadomości SMS do zewnętrznego serwisu.
     */
    @Test
    void shouldSendSmsSuccessfully() {
        // Definiowanie zachowania Mocka
        when(config.getExternalSmsUrl()).thenReturn(apiUrl);
        ResponseEntity<String> responseEntity = ResponseEntity.ok("SMS wysłany pomyślnie");
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity);

        // Wywołanie metody
        externalSmsSender.sendSms(smsRequest);

        // Sprawdzenie, czy zapytanie zostało wykonane
        verify(restTemplate, times(1)).exchange(eq(apiUrl), eq(HttpMethod.POST), any(), eq(String.class));
    }

    /**
     * Testuje obsługę błędu HTTP podczas wysyłania SMS.
     */
    @Test
    void shouldHandleSmsSendingError() {
        // Definiowanie zachowania Mocka
        when(config.getExternalSmsUrl()).thenReturn(apiUrl);
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Błąd wysyłania"));

        // Wywołanie metody
        externalSmsSender.sendSms(smsRequest);

        // Sprawdzenie, czy zapytanie zostało wykonane
        verify(restTemplate, times(1)).exchange(eq(apiUrl), eq(HttpMethod.POST), any(), eq(String.class));
    }
}