package com.example.playdemo.service;

import com.example.playdemo.model.User;
import com.example.playdemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientService clientService;

    /**
     * Testuje pobieranie statusu klienta.
     */
    @Test
    void shouldReturnClientStatus() {
        // Przygotowanie danych
        String phone = "123456789";
        User user = new User();
        user.setPhoneNumber(phone);
        user.setMessageChecked(5);
        user.setPaidMessages(3);

        // Definiowanie zachowania Mocka
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(user));

        // Wywołanie metody serwisu
        Optional<Map<String, Object>> response = clientService.getClientStatus(phone);

        // Sprawdzenie wyniku
        assertThat(response).isPresent();
        assertThat(response.get()).containsEntry("phone", phone);
        assertThat(response.get()).containsEntry("messagesToPay", 2);
    }

    /**
     * Testuje odpowiedź dla nieznanego klienta.
     */
    @Test
    void shouldReturnEmptyForUnknownClient() {
        // Przygotowanie danych
        String phone = "999999999";

        // Definiowanie zachowania Mocka
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        // Wywołanie metody serwisu
        Optional<Map<String, Object>> response = clientService.getClientStatus(phone);

        // Sprawdzenie wyniku
        assertThat(response).isEmpty();
    }

    /**
     * Testuje poprawną obsługę płatności za wiadomości.
     */
    @Test
    void shouldProcessPaymentSuccessfully() {
        // Przygotowanie danych
        String phone = "123456789";
        User user = new User();
        user.setPhoneNumber(phone);
        user.setMessageChecked(5);
        user.setPaidMessages(3);

        // Definiowanie zachowania Mocka
        ReflectionTestUtils.setField(clientService, "paymentSuccessfullyProcessed", "Payment1 processed successfully");
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock zapisania użytkownika

        // Wywołanie metody serwisu
        Optional<Map<String, String>> response = clientService.payMessages(phone);

        // Sprawdzenie wyniku
        assertThat(response).isPresent();
        assertThat(response.get()).containsEntry("message", "Payment1 processed successfully");

        // Sprawdzenie, czy użytkownik został zapisany z nową wartością opłaconych wiadomości
        verify(userRepository, times(1)).save(argThat(u -> u.getPaidMessages() == 5));
    }

    /**
     * Testuje odpowiedź dla nieznanego klienta podczas płatności.
     */
    @Test
    void shouldReturnEmptyForUnknownClientPayment() {
        // Przygotowanie danych
        String phone = "999999999";

        // Definiowanie zachowania Mocka
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        // Wywołanie metody serwisu
        Optional<Map<String, String>> response = clientService.payMessages(phone);

        // Sprawdzenie wyniku
        assertThat(response).isEmpty();
    }
}