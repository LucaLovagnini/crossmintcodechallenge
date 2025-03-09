package com.crossmint.challenge.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record GoalMap(@Positive int rows, @Positive int cols, @NotNull Set<ApiSerializable> astralObjects) {
}