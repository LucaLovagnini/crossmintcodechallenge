package com.crossmint.challenge.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all astral objects in the Crossmint challenge.
 * This class implements common functionality shared by all astral objects
 * such as position (row and column) and API serialization.
 * <p>
 * All concrete astral objects should extend this class and implement
 * any type-specific attributes and behavior.
 */
@Getter
@RequiredArgsConstructor
@ToString
public abstract class AbstractAstralObject implements AstralObject, ApiSerializable {

    /**
     * The row coordinate of this astral object in the grid.
     * Must be a non-negative integer.
     */
    @Min(value = 0, message = "Row must be a non-negative integer")
    private final int row;

    /**
     * The column coordinate of this astral object in the grid.
     * Must be a non-negative integer.
     */
    @Min(value = 0, message = "Column must be a non-negative integer")
    private final int column;

    /**
     * The API path used for creating this type of astral object.
     * This path is appended to the base API URL when making requests.
     */
    @NotNull
    private final String creationPath;

    /**
     * Converts this astral object into a request body map suitable for API requests.
     * The map includes the common attributes (row, column, candidateId) and any
     * object-specific attributes added by the subclass.
     *
     * @param candidateId The candidate ID to include in the request
     * @return A map containing all attributes needed for the API request
     */
    @Override
    public Map<String, Object> toRequestBody(String candidateId) {
        Map<String, Object> body = new HashMap<>();
        body.put("row", row);
        body.put("column", column);
        body.put("candidateId", candidateId);
        addSpecificAttributes(body);
        return body;
    }

    /**
     * Hook method for subclasses to add type-specific attributes to the request body.
     * The default implementation adds no additional attributes.
     * Subclasses should override this method to add their specific attributes.
     *
     * @param body The request body map to which attributes should be added
     */
    protected void addSpecificAttributes(Map<String, Object> body) {
        // Default is no extra attributes
    }
}