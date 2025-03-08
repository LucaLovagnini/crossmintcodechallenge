package com.crossmint.challenge.config;

import jakarta.validation.constraints.Positive;

public record GoalMap(@Positive int rows, @Positive int cols) {
}