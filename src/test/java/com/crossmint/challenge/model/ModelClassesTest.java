package com.crossmint.challenge.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelClassesTest {

    private static final String TEST_CANDIDATE_ID = "test-candidate-id";

    @Test
    void testPolyanetCreation() {
        Polyanet polyanet = new Polyanet(3, 5);

        assertEquals(3, polyanet.getRow());
        assertEquals(5, polyanet.getColumn());
        assertEquals("/polyanets", polyanet.getCreationPath());

        Map<String, Object> requestBody = polyanet.toRequestBody(TEST_CANDIDATE_ID);
        assertEquals(3, requestBody.get("row"));
        assertEquals(5, requestBody.get("column"));
        assertEquals(TEST_CANDIDATE_ID, requestBody.get("candidateId"));
        assertEquals(3, requestBody.size()); // No additional attributes
    }

    @Test
    void testComethCreation() {
        Cometh cometh = new Cometh(2, 4, ComethDirection.UP);

        assertEquals(2, cometh.getRow());
        assertEquals(4, cometh.getColumn());
        assertEquals(ComethDirection.UP, cometh.getDirection());
        assertEquals("/comeths", cometh.getCreationPath());

        Map<String, Object> requestBody = cometh.toRequestBody(TEST_CANDIDATE_ID);
        assertEquals(2, requestBody.get("row"));
        assertEquals(4, requestBody.get("column"));
        assertEquals(TEST_CANDIDATE_ID, requestBody.get("candidateId"));
        assertEquals("up", requestBody.get("direction"));
        assertEquals(4, requestBody.size());
    }

    @Test
    void testSoloonCreation() {
        Soloon soloon = new Soloon(1, 6, SoloonColor.BLUE);

        assertEquals(1, soloon.getRow());
        assertEquals(6, soloon.getColumn());
        assertEquals(SoloonColor.BLUE, soloon.getColor());
        assertEquals("/soloons", soloon.getCreationPath());

        Map<String, Object> requestBody = soloon.toRequestBody(TEST_CANDIDATE_ID);
        assertEquals(1, requestBody.get("row"));
        assertEquals(6, requestBody.get("column"));
        assertEquals(TEST_CANDIDATE_ID, requestBody.get("candidateId"));
        assertEquals("blue", requestBody.get("color"));
        assertEquals(4, requestBody.size());
    }

    @ParameterizedTest
    @EnumSource(ComethDirection.class)
    void testComethDirectionFromString(ComethDirection direction) {
        String directionStr = direction.getApiValue();
        ComethDirection parsedDirection = ComethDirection.fromString(directionStr);

        assertEquals(direction, parsedDirection);
    }

    @Test
    void testComethDirectionFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ComethDirection.fromString("invalid"));
    }

    @ParameterizedTest
    @EnumSource(SoloonColor.class)
    void testSoloonColorFromString(SoloonColor color) {
        String colorStr = color.getApiValue();
        SoloonColor parsedColor = SoloonColor.fromString(colorStr);

        assertEquals(color, parsedColor);
    }

    @Test
    void testSoloonColorFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> SoloonColor.fromString("invalid"));
    }

    @Test
    void testGoalMapCreation() {
        Set<ApiSerializable> astralObjects = Set.of(
                new Polyanet(0, 0),
                new Cometh(1, 1, ComethDirection.DOWN),
                new Soloon(2, 2, SoloonColor.RED)
        );

        GoalMap goalMap = new GoalMap(3, 3, astralObjects);

        assertEquals(3, goalMap.rows());
        assertEquals(3, goalMap.cols());
        assertEquals(3, goalMap.astralObjects().size());

        // Instead of using contains(), check for an object with matching coordinates
        boolean foundPolyanet = goalMap.astralObjects().stream()
                .anyMatch(obj -> obj instanceof Polyanet &&
                        ((Polyanet) obj).getRow() == 0 &&
                        ((Polyanet) obj).getColumn() == 0);

        assertTrue(foundPolyanet, "Should contain a Polyanet at (0,0)");
    }
}