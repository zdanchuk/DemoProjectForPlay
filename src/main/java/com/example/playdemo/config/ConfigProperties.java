package com.example.playdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sms-service")
@Getter
@Setter
public class ConfigProperties {
    private String externalSmsUrl;
    private String webRiskApiUrl;
    private List<String> reservedNumbers;
    private List<String> threatTypes;
    private List<String> confidenceLevel;
}