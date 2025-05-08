package com.example.playdemo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WebRiskResponse {
    private List<Score> scores;
}