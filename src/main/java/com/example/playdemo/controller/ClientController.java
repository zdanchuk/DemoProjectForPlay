package com.example.playdemo.controller;

import com.example.playdemo.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Kontroler REST API do obsługi klientów.
 */
@RestController
@RequestMapping("/client")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Pobiera status klienta.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getClientStatus(@RequestParam String phone) {
        Optional<Map<String, Object>> response = clientService.getClientStatus(phone);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Użytkownik nie znaleziony")));
    }

    /**
     * Obsługuje płatność za wiadomości.
     */
    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> payMessages(@RequestParam String phone) {
        Optional<Map<String, String>> response = clientService.payMessages(phone);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Użytkownik nie znaleziony")));
    }
}