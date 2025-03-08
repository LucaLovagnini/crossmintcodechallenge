package com.crossmint.challenge.model;

import java.util.Map;

public interface ApiSerializable {
    String getCreationPath();
    Map<String, Object> toRequestBody(String candidateId);
}
