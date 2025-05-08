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
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class SmsService {
    private final UserRepository userRepository;
    private final SmsTemplateRepository templateRepository;
    private final RestTemplate restTemplate;
    private final ExternalSmsSender smsSender;
    private final ConfigProperties config;

    public SmsService(UserRepository userRepository, SmsTemplateRepository templateRepository,
                      RestTemplate restTemplate, ExternalSmsSender smsSender, ConfigProperties config) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.restTemplate = restTemplate;
        this.smsSender = smsSender;
        this.config = config;
    }

    /**
     * Główna metoda przetwarzania wiadomości SMS.
     * Obsługuje polecenia START/STOP oraz sprawdzanie linków phishingowych.
     */
    public void processSms(SmsRequest smsRequest) {
        String sender = smsRequest.getSender();
        String recipient = smsRequest.getRecipient();
        String message = smsRequest.getMessage();

        // Sprawdzenie, czy wiadomość została wysłana na zarezerwowany numer
        if (config.getReservedNumbers().contains(recipient)) {
            if ("START".equalsIgnoreCase(message)) {
                handleStart(sender);
            } else if ("STOP".equalsIgnoreCase(message)) {
                handleStop(sender);
            }
        } else {
            handleOtherMessages(sender, recipient, message);
        }
    }

    /**
     * Obsługuje komendę START – aktywuje usługę dla użytkownika lub informuje o wcześniejszej aktywacji.
     */
    private void handleStart(String sender) {
        User user = userRepository.findByPhoneNumber(sender).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhoneNumber(sender);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setMessageChecked(0);
            newUser.setPaidMessages(0);
            userRepository.save(newUser);
            sendSms(config.getReservedNumbers().getFirst(), sender, "activation_template"); // Wysłanie SMS o aktywacji
            return newUser;
        });

        if (user.getStatus() == UserStatus.INACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            sendSms(config.getReservedNumbers().getFirst(), sender, "reactivation_template"); // Wysłanie SMS o reaktywacji
        } else {
            sendSms(config.getReservedNumbers().getFirst(), sender, "already_active_template"); // Użytkownik już aktywny
        }
    }

    /**
     * Obsługuje komendę STOP – dezaktywuje usługę lub informuje użytkownika, że nie była aktywowana.
     */
    private void handleStop(String sender) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(sender);

        if (userOpt.isEmpty()) {
            sendSms(config.getReservedNumbers().getFirst(), sender, "not_active_template"); // Usługa nigdy nie była aktywowana
            return;
        }

        User user = userOpt.get();
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
            sendSms(config.getReservedNumbers().getFirst(), sender, "deactivation_template"); // Wysłanie SMS o dezaktywacji
        } else {
            sendSms(config.getReservedNumbers().getFirst(), sender, "already_inactive_template"); // Usługa była już dezaktywowana
        }
    }

    /**
     * Sprawdza, czy wiadomość zawiera link do strony internetowej.
     */
    private boolean containsUrl(String message) {
        return message.matches(".*https?://\\S+.*");
    }

    /**
     * Sprawdza, czy numer odbiorcy należy do aktywnego użytkownika w systemie.
     */
    private boolean isRecipientActive(String recipient) {
        return userRepository.findByPhoneNumber(recipient)
                .map(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }

    /**
     * Asynchronicznie sprawdza wiadomość pod kątem phishingu i podejmuje odpowiednie działania.
     * Jeśli wiadomość zawiera URL i odbiorca jest aktywny, wysyła żądanie do API WebRisk,
     * aby sprawdzić poziom zagrożenia. Jeśli wiadomość jest bezpieczna, zostaje wysłana do odbiorcy.
     *
     * @param sender    Nadawca wiadomości.
     * @param recipient Odbiorca wiadomości.
     * @param message   Treść wiadomości do sprawdzenia.
     */
    @Async
    public void handleOtherMessages(String sender, String recipient, String message) {
        // Sprawdzenie, czy wiadomość zawiera URL oraz czy odbiorca jest aktywny
        if (!containsUrl(message) || !isRecipientActive(recipient)) {
            return; // Jeśli warunki nie są spełnione, kończymy metodę
        }

        String apiUrl = config.getWebRiskApiUrl();

        // Przygotowanie zapytania do API WebRisk w celu analizy zagrożenia phishingowego
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("uri", message); // URL do analizy
        requestBody.put("threatTypes", config.getThreatTypes()); // Typy zagrożeń do sprawdzenia
        requestBody.put("allowScan", true); // Wymagany parametr API

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Asynchroniczne wywołanie API WebRisk w tle
        CompletableFuture.supplyAsync(() -> {
            ResponseEntity<WebRiskResponse> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, WebRiskResponse.class);

            updateMessageChecked(recipient);
            // Sprawdzamy odpowiedź API i analizujemy poziom zagrożenia
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Score> scores = response.getBody().getScores();
                for (Score score : scores) {
                    if (!config.getConfidenceLevel().contains(score.getConfidenceLevel())) {
                        return true; // Jeśli wykryto phishing, blokujemy wiadomość
                    }
                }
                return false; // Brak zagrożenia phishingowego
            }
            return true; // Jeśli API nie działa lub opowiada błędem, to też blokujemy wiadomość
        }).thenAccept(isPhishing -> {
            if (!isPhishing) {
                sendSms(sender, recipient, message); // Jeśli wiadomość jest bezpieczna, wysyłamy SMS
            } else {
                System.out.println("Wykryto phishing: " + message); // Logowanie wykrycia phishingu
            }
        });
    }

    /**
     * Aktualizuje liczbę sprawdzonych wiadomości dla użytkownika.
     */
    private void updateMessageChecked(String phone) {
        userRepository.findByPhoneNumber(phone).ifPresent(user -> {
            user.setMessageChecked(user.getMessageChecked() + 1);
            userRepository.save(user);
        });
    }

    /**
     * Pobiera szablon wiadomości z bazy i wysyła SMS przez zewnętrzny serwis.
     */
    private void sendSms(String sender, String recipient, String templateKey) {
        SmsTemplate template = templateRepository.findByTemplateKey(templateKey);
        if (template != null) {
            smsSender.sendSms(new SmsRequest(sender, recipient, template.getMessage()));
        }
    }
}


