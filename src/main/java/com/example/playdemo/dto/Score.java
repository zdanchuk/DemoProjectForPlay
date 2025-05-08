package com.example.playdemo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Score {
    private String threatType;
    private String confidenceLevel;
}
