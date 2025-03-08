package com.crossmint.challenge.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class AbstractAstralObject implements AstralObject, ApiSerializable {

    @Min(value = 0, message = "Row must be a non-negative integer")
    private final int row;

    @Min(value = 0, message = "Column must be a non-negative integer")
    private final int column;

    @NotNull
    private final String creationPath;

    @Override
    public Map<String, Object> toRequestBody(String candidateId) {
        Map<String, Object> body = new HashMap<>();
        body.put("row", row);
        body.put("column", column);
        body.put("candidateId", candidateId);
        addSpecificAttributes(body);
        return body;
    }

    protected void addSpecificAttributes(Map<String, Object> body) {
        // Default is no extra attributes
    }
}
