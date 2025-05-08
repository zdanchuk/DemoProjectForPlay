package com.example.playdemo.controller;

import com.example.playdemo.dto.SmsRequest;
import com.example.playdemo.service.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SmsController.class)
@ExtendWith(MockitoExtension.class)
public class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SmsService smsService;

    /**
     * Testuje poprawne przetwarzanie wiadomości SMS przez kontroler.
     */
    @Test
    void shouldProcessSmsSuccessfully() throws Exception {
        // Definiowanie zachowania Mocka
        willDoNothing().given(smsService).processSms(Mockito.any(SmsRequest.class));

        // Wywołanie zapytania do API
        mockMvc.perform(post("/sms/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sender\":\"123456789\",\"recipient\":\"987654321\",\"message\":\"START\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Wiadomość została przetworzona"));
    }
}