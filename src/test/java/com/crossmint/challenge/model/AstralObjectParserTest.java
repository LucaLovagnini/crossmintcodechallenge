package com.crossmint.challenge.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AstralObjectParserTest {

    @Test
    void testParseEmptyGoal() {
        List<List<String>> emptyGoal = List.of();

        assertThrows(IllegalStateException.class, () -> AstralObjectParser.parseAstralObjects(emptyGoal));
    }

    @Test
    void testParseGoalWithEmptyFirstRow() {
        List<List<String>> goalWithEmptyFirstRow = List.of(List.of());

        assertThrows(IllegalStateException.class, () -> AstralObjectParser.parseAstralObjects(goalWithEmptyFirstRow));
    }

    @Test
    void testParseSimpleGoal() {
        // Create a simple goal grid with one of each object type
        List<List<String>> simpleGoal = List.of(
                List.of("POLYANET", "SPACE", "UP_COMETH", "WHITE_SOLOON"),
                List.of("SPACE", "DOWN_COMETH", "SPACE", "BLUE_SOLOON"),
                List.of("LEFT_COMETH", "SPACE", "RIGHT_COMETH", "RED_SOLOON"),
                List.of("SPACE", "SPACE", "SPACE", "PURPLE_SOLOON")
        );

        Set<ApiSerializable> astralObjects = AstralObjectParser.parseAstralObjects(simpleGoal);

        // We should have 9 objects (excluding "SPACE")
        assertEquals(9, astralObjects.size());

        // Verify we have the expected types
        long polyanetCount = astralObjects.stream()
                .filter(obj -> obj instanceof Polyanet)
                .count();
        assertEquals(1, polyanetCount);

        long comethCount = astralObjects.stream()
                .filter(obj -> obj instanceof Cometh)
                .count();
        assertEquals(4, comethCount);

        long soloonCount = astralObjects.stream()
                .filter(obj -> obj instanceof Soloon)
                .count();
        assertEquals(4, soloonCount);

        // Verify some specific objects
        assertTrue(astralObjects.stream()
                .anyMatch(obj -> obj instanceof Polyanet && ((Polyanet) obj).getRow() == 0 && ((Polyanet) obj).getColumn() == 0));

        assertTrue(astralObjects.stream()
                .anyMatch(obj -> obj instanceof Cometh &&
                        ((Cometh) obj).getRow() == 0 &&
                        ((Cometh) obj).getColumn() == 2 &&
                        ((Cometh) obj).getDirection() == ComethDirection.UP));

        assertTrue(astralObjects.stream()
                .anyMatch(obj -> obj instanceof Soloon &&
                        ((Soloon) obj).getRow() == 3 &&
                        ((Soloon) obj).getColumn() == 3 &&
                        ((Soloon) obj).getColor() == SoloonColor.PURPLE));
    }

    @Test
    void testParseGoalWithInvalidObjectType() {
        List<List<String>> goalWithInvalidType = List.of(
                List.of("POLYANET", "INVALID_TYPE")
        );

        assertThrows(IllegalArgumentException.class, () -> AstralObjectParser.parseAstralObjects(goalWithInvalidType));
    }

    @Test
    void testParseGoalWithAllSpaces() {
        List<List<String>> allSpacesGoal = List.of(
                List.of("SPACE", "SPACE"),
                List.of("SPACE", "SPACE")
        );

        Set<ApiSerializable> astralObjects = AstralObjectParser.parseAstralObjects(allSpacesGoal);

        // Should have no objects
        assertTrue(astralObjects.isEmpty());
    }

    @Test
    void testObjectCoordinatesAreCorrect() {
        List<List<String>> goal = List.of(
                List.of("POLYANET", "BLUE_SOLOON"),
                List.of("UP_COMETH", "PURPLE_SOLOON")
        );

        Set<ApiSerializable> astralObjects = AstralObjectParser.parseAstralObjects(goal);

        assertEquals(4, astralObjects.size());

        // Check each object has the correct coordinates
        astralObjects.forEach(obj -> {
            int row = ((AstralObject) obj).getRow();
            int col = ((AstralObject) obj).getColumn();

            if (row == 0 && col == 0) {
                assertInstanceOf(Polyanet.class, obj);
            } else if (row == 0 && col == 1) {
                assertInstanceOf(Soloon.class, obj);
                assertEquals(SoloonColor.BLUE, ((Soloon) obj).getColor());
            } else if (row == 1 && col == 0) {
                assertInstanceOf(Cometh.class, obj);
                assertEquals(ComethDirection.UP, ((Cometh) obj).getDirection());
            } else if (row == 1 && col == 1) {
                assertInstanceOf(Soloon.class, obj);
                assertEquals(SoloonColor.PURPLE, ((Soloon) obj).getColor());
            } else {
                fail("Unexpected coordinates: (" + row + ", " + col + ")");
            }
        });
    }
}