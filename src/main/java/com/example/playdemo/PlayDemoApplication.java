package com.example.playdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PlayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayDemoApplication.class, args);
    }


    /**
     * Tworzy bean dla RestTemplate, aby można było go autowire w innych klasach.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
