package com.crossmint.challenge.model;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public class AstralObjectParser {
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
