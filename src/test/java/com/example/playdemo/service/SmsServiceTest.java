package com.example.playdemo.service;

import com.example.playdemo.config.ConfigProperties;
import com.example.playdemo.dto.Score;
import com.example.playdemo.dto.SmsRequest;
import com.example.playdemo.dto.WebRiskResponse;
import com.example.playdemo.model.SmsTemplate;
import com.example.playdemo.model.User;
import com.example.playdemo.model.enums.UserStatus;
import com.example.playdemo.repository.SmsTemplateRepository;
import com.example.playdemo.repository.UserRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmsServiceTest {

    @Mock
    private static UserRepository userRepository;

    @Mock
    private ExternalSmsSender smsSender;

    @Mock
    private static ConfigProperties config;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private static SmsTemplateRepository templateRepository;

    @InjectMocks
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(config.getWebRiskApiUrl()).thenReturn("riskCheck.site");
        Mockito.lenient().when(config.getConfidenceLevel()).thenReturn(List.of("SAFE", "LOW", "MEDIUM"));
        Mockito.lenient().when(config.getReservedNumbers()).thenReturn(List.of("00000"));
        Mockito.lenient().when(templateRepository.findByTemplateKey(anyString())).thenReturn(new SmsTemplate());
        Mockito.lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Testuje aktywację użytkownika po otrzymaniu SMS "START".
     */
    @Test
    void shouldActivateUserOnStartCommand() {
        // Przygotowanie danych
        String sender = "123456789";
        SmsRequest smsRequest = new SmsRequest(sender, "00000", "START");
        User user = new User();
        user.setPhoneNumber(sender);
        user.setStatus(UserStatus.INACTIVE);

        // Definiowanie zachowania Mocka
        when(userRepository.findByPhoneNumber(sender)).thenReturn(Optional.of(user));

        // Wywołanie metody serwisu
        smsService.processSms(smsRequest);

        // Sprawdzenie, czy użytkownik został aktywowany
        verify(userRepository, times(1)).save(argThat(u -> u.getStatus() == UserStatus.ACTIVE));
        verify(smsSender, times(1)).sendSms(any(SmsRequest.class)); // Sprawdzenie wysłania SMS
    }

    /**
     * Testuje dezaktywację użytkownika po otrzymaniu SMS "STOP".
     */
    @Test
    void shouldDeactivateUserOnStopCommand() {
        // Przygotowanie danych
        String sender = "123456789";
        SmsRequest smsRequest = new SmsRequest(sender, "00000", "STOP");
        User user = new User();
        user.setPhoneNumber(sender);
        user.setStatus(UserStatus.ACTIVE);

        // Definiowanie zachowania Mocka
        when(userRepository.findByPhoneNumber(sender)).thenReturn(Optional.of(user));
//        when(config.getReservedNumbers()).thenReturn(List.of("00000"));
//        when(templateRepository.findByTemplateKey(anyString())).thenReturn(new SmsTemplate());
//        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Wywołanie metody serwisu
        smsService.processSms(smsRequest);

        // Sprawdzenie, czy użytkownik został dezaktywowany
        verify(userRepository, times(1)).save(any());
        verify(smsSender, times(1)).sendSms(any(SmsRequest.class)); // Sprawdzenie wysłania SMS
    }


    /**
     * Test sprawdza, czy wiadomość jest wysyłana, gdy nie wykryto phishingu.
     */
    @Test
    void testHandleOtherMessages_NoPhishing_MessageSent() {
        // Ustalanie danych wejściowych
        String sender = "123456789";
        String recipient = "987654321";
        String message = "Hello, visit https://safe-site.com";

        // Konfiguracja zachowania metod mock
        User user = new User();
        user.setPhoneNumber(recipient);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByPhoneNumber(recipient)).thenReturn(Optional.of(user));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(WebRiskResponse.class)))
                .thenReturn(createPhishingResponse("SAFE")); // Symulacja odpowiedzi API

        // Wywołanie metody testowanej
        smsService.handleOtherMessages(sender, recipient, message);

        // Oczekiwanie na asynchroniczne wykonanie metody
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(smsSender, times(1)).sendSms(any(SmsRequest.class)));
    }


    /**
     * Test sprawdza, czy wiadomość nie jest wysyłana, gdy wykryto phishing.
     */
    @Test
    void testHandleOtherMessages_Phishing_Detected() {
        // Ustalanie danych wejściowych
        String sender = "123456789";
        String recipient = "987654321";
        String message = "Malicious site: https://dangerous-site.com";

        // Konfiguracja zachowania metod mock
        User user = new User();
        user.setPhoneNumber(recipient);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByPhoneNumber(recipient)).thenReturn(Optional.of(user));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(WebRiskResponse.class)))
                .thenReturn(createPhishingResponse("HIGH")); // Symulacja odpowiedzi API


        // Wywołanie metody testowanej
        smsService.handleOtherMessages(sender, recipient, message);

        // Oczekiwanie na asynchroniczne wykonanie metody
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(smsSender, never()).sendSms(any(SmsRequest.class)));

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(WebRiskResponse.class));
    }

    /**
     * Symuluje odpowiedź API WebRisk, wskazując na phishing.
     */
    private ResponseEntity<WebRiskResponse> createPhishingResponse(String level) {
        WebRiskResponse response = new WebRiskResponse();
        response.setScores(java.util.List.of(new Score("MALWARE", level))); // Poziom zagrożenia wskazuje na phishing
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}