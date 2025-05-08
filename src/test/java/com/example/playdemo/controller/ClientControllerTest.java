package com.example.playdemo.controller;

import com.example.playdemo.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    /**
     * Testuje poprawne pobranie statusu klienta.
     */
    @Test
    void shouldReturnClientStatusSuccessfully() throws Exception {
        // Przygotowanie danych
        String phone = "+48123456789";
        Map<String, Object> response = Map.of("phone", phone, "messagesToPay", 2);

        // Definiowanie zachowania Mocka
        given(clientService.getClientStatus(Mockito.anyString())).willReturn(Optional.of(response));

        // Wywołanie zapytania do API
        mockMvc.perform(get("/client/status")
                        .param("phone", phone)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.messagesToPay").value(2));
    }

    /**
     * Testuje odpowiedź 404 dla nieznanego numeru klienta.
     */
    @Test
    void shouldReturnNotFoundForUnknownClient() throws Exception {
        // Przygotowanie danych
        String phone = "+48999999999";

        // Definiowanie zachowania Mocka
        given(clientService.getClientStatus(Mockito.anyString())).willReturn(Optional.empty());

        // Wywołanie zapytania do API
        mockMvc.perform(get("/client/status")
                        .param("phone", phone)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Użytkownik nie znaleziony"));
    }

    /**
     * Testuje poprawną obsługę płatności za wiadomości.
     */
    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Przygotowanie danych
        String phone = "+48123456789";
        Map<String, String> response = Map.of("message", "Płatność została pomyślnie przetworzona");

        // Definiowanie zachowania Mocka
        given(clientService.payMessages(Mockito.anyString())).willReturn(Optional.of(response));

        // Wywołanie zapytania do API
        mockMvc.perform(post("/client/pay")
                        .param("phone", phone)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Płatność została pomyślnie przetworzona"));
    }

    /**
     * Testuje odpowiedź 404 dla próby płatności przez nieznanego klienta.
     */
    @Test
    void shouldReturnNotFoundForUnknownClientPayment() throws Exception {
        // Przygotowanie danych
        String phone = "+48999999999";

        // Definiowanie zachowania Mocka
        given(clientService.payMessages(Mockito.anyString())).willReturn(Optional.empty());

        // Wywołanie zapytania do API
        mockMvc.perform(post("/client/pay")
                        .param("phone", phone)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Użytkownik nie znaleziony"));
    }
}