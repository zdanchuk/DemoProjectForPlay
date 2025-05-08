package com.example.playdemo.service;

import com.example.playdemo.model.User;
import com.example.playdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Serwis obsługujący logikę biznesową klientów.
 */
@Service
public class ClientService {
    private final UserRepository userRepository;
    @Value("${service.requires.payment}")
    private String serviceRequirePayment;
    @Value("${everything.paid}")
    private String everythingPaid;
    @Value("${payment.successfully.processed}")
    private String paymentSuccessfullyProcessed;

    public ClientService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Pobiera status klienta na podstawie numeru telefonu.
     * @param phone Numer telefonu klienta
     * @return Informacje o konieczności płatności
     */
    public Optional<Map<String, Object>> getClientStatus(String phone) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(phone);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("phone", user.getPhoneNumber());

        if (user.getMessageChecked() > user.getPaidMessages()) {
            response.put("message", serviceRequirePayment);
            response.put("messagesToPay", user.getMessageChecked() - user.getPaidMessages());
        } else {
            response.put("message", everythingPaid);
        }

        return Optional.of(response);
    }

    /**
     * Aktualizuje liczbę opłaconych wiadomości dla klienta.
     * @param phone Numer telefonu klienta
     * @return Informacja o statusie płatności
     */
    public Optional<Map<String, String>> payMessages(String phone) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(phone);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        user.setPaidMessages(user.getMessageChecked());
        userRepository.save(user);

        return Optional.of(Map.of("message", paymentSuccessfullyProcessed));
    }
}