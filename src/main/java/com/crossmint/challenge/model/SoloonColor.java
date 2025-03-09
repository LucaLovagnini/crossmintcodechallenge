package com.crossmint.challenge.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SoloonColor {
    WHITE("white"),
    BLUE("blue"),
    RED("red"),
    PURPLE("purple");

    private final String apiValue;

    SoloonColor(String apiValue) {
        this.apiValue = apiValue;
    }

    public static SoloonColor fromString(String value) {
        return Arrays.stream(SoloonColor.values())
                .filter(color -> color.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Soloon color: " + value));
    }
}
