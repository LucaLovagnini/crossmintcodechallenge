package com.crossmint.challenge.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ComethDirection {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right");

    private final String apiValue;

    ComethDirection(String apiValue) {
        this.apiValue = apiValue;
    }

    public static ComethDirection fromString(String value) {
        return Arrays.stream(ComethDirection.values())
                .filter(dir -> dir.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Cometh direction: " + value));
    }
}
