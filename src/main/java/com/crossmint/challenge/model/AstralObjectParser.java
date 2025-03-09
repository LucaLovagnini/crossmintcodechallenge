package com.crossmint.challenge.model;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for parsing goal map data into astral object entities.
 * <p>
 * This parser converts the 2D grid representation of the goal map into a set of
 * concrete astral object instances (Polyanet, Cometh, Soloon) that can be used for
 * API operations.
 * <p>
 * The parser assumes that all rows in the input grid have the same number of columns
 * as the first row.
 */
@UtilityClass
public class AstralObjectParser {

    /**
     * Parses a 2D grid of string types into a set of astral objects. This is especially
     * handy when we want to parse the current grid from the /map API.
     * <p>
     * This method processes each cell in the grid, creating the appropriate astral
     * object based on the type string. Cells containing "SPACE" are ignored.
     * <p>
     * IMPORTANT: This method assumes that all rows have the same number of columns
     * as the first row.
     *
     * @param goal A 2D list representing the grid of astral objects, where each string
     *            identifies the type of object at that position
     * @return A set of astral objects created from the non-empty cells in the grid
     * @throws IllegalStateException if the grid is empty or the first row is empty
     * @throws IllegalArgumentException if an unknown astral object type is encountered
     */
    public Set<ApiSerializable> parseAstralObjects(List<List<String>> goal) {
        if (goal.isEmpty() || goal.getFirst().isEmpty()) {
            throw new IllegalStateException("Invalid goal response: Empty grid received");
        }

        Set<ApiSerializable> astralObjects = new HashSet<>();

        for (int i = 0; i < goal.size(); i++) {
            for (int j = 0; j < goal.getFirst().size(); j++) {
                String type = goal.get(i).get(j);

                if (!type.equals("SPACE")) {
                    astralObjects.add(AstralObjectParser.createAstralObject(type, i, j));
                }
            }
        }

        return astralObjects;
    }

    /**
     * Creates a specific astral object instance based on its type string.
     * <p>
     * This method matches the type string to the appropriate astral object class
     * and constructs an instance with the given coordinates and any type-specific
     * properties (like direction for Cometh or color for Soloon).
     *
     * @param type The string identifying the type of astral object
     * @param row The row coordinate in the grid
     * @param col The column coordinate in the grid
     * @return A concrete astral object instance implementing the ApiSerializable interface
     * @throws IllegalArgumentException if the type string doesn't match any known astral object
     */
    private static ApiSerializable createAstralObject(String type, int row, int col) {
        return switch (type) {
            case "POLYANET" -> new Polyanet(row, col);
            case "UP_COMETH", "DOWN_COMETH", "LEFT_COMETH", "RIGHT_COMETH" ->
                    new Cometh(row, col, ComethDirection.fromString(type.replace("_COMETH", "").toLowerCase()));
            case "WHITE_SOLOON", "BLUE_SOLOON", "RED_SOLOON", "PURPLE_SOLOON" ->
                    new Soloon(row, col, SoloonColor.fromString(type.replace("_SOLOON", "").toLowerCase()));
            default -> throw new IllegalArgumentException("Unknown astral object: " + type);
        };
    }
}