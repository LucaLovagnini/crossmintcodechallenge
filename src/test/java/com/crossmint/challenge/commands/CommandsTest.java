package com.crossmint.challenge.commands;

import com.crossmint.challenge.commands.create.CreateComethCommand;
import com.crossmint.challenge.commands.create.CreateSoloonCommand;
import com.crossmint.challenge.commands.delete.DeleteAllCommand;
import com.crossmint.challenge.commands.delete.DeleteCommand;
import com.crossmint.challenge.model.*;
import com.crossmint.challenge.service.AstralObjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommandsTest {

    @Mock
    private AstralObjectService astralObjectService;

    @BeforeEach
    void setUp() {
        // Mock the goal map (now marked as lenient)
        Set<ApiSerializable> objects = new HashSet<>();
        objects.add(new Polyanet(0, 0));
        GoalMap goalMap = new GoalMap(10, 10, objects);
        lenient().when(astralObjectService.getGoalMap()).thenReturn(goalMap);
    }

    @Test
    void testCreateComethCommand() {
        // Create the command
        CreateComethCommand createComethCommand = CreateComethCommand.builder()
                .service(astralObjectService)
                .direction(ComethDirection.UP)
                .x(1)
                .y(2)
                .build();

        // Mock the service call
        doNothing().when(astralObjectService).processAstralObject(any(Cometh.class), eq(HttpMethod.POST));

        // Execute
        createComethCommand.run();

        // Verify the service was called with the correct parameters
        verify(astralObjectService).processAstralObject(
                any(Cometh.class),
                eq(HttpMethod.POST)
        );
    }

    @Test
    void testCreateSoloonCommand() {
        // Create the command
        CreateSoloonCommand createSoloonCommand = CreateSoloonCommand.builder()
                .service(astralObjectService)
                .color(SoloonColor.BLUE)
                .x(1)
                .y(2)
                .build();

        // Mock the service call
        doNothing().when(astralObjectService).processAstralObject(any(Soloon.class), eq(HttpMethod.POST));

        // Execute
        createSoloonCommand.run();

        // Verify the service was called with the correct parameters
        verify(astralObjectService).processAstralObject(
                any(Soloon.class),
                eq(HttpMethod.POST)
        );
    }

    @Test
    void testCreateComethCommandWithInvalidCoordinates() {
        // Create the command
        CreateComethCommand createComethCommand = CreateComethCommand.builder()
                .service(astralObjectService)
                .direction(ComethDirection.UP)
                .x(20)
                .y(5)
                .build();

        // Execute
        createComethCommand.run();

        // Verify the service was NOT called
        verify(astralObjectService, never()).processAstralObject(any(), any());
    }

    @Test
    void testCreateSoloonCommandWithInvalidCoordinates() {
        // Create the command
        CreateSoloonCommand createSoloonCommand = CreateSoloonCommand.builder()
                .service(astralObjectService)
                .color(SoloonColor.BLUE)
                .x(20)
                .y(5)
                .build();

        // Execute
        createSoloonCommand.run();

        // Verify the service was NOT called
        verify(astralObjectService, never()).processAstralObject(any(), any());
    }

    @Test
    void testReplicateGoalCommand() {
        // Create the command
        ReplicateGoalCommand replicateGoalCommand = ReplicateGoalCommand.builder()
                .service(astralObjectService)
                .build();

        // Execute
        replicateGoalCommand.run();

        // Verify the service method was called
        verify(astralObjectService).replicateGoalMap();
    }

    @Test
    void testDeleteAllCommand() {
        // Create the command
        DeleteAllCommand deleteAllCommand = DeleteAllCommand.builder()
                .service(astralObjectService)
                .build();

        // Execute
        deleteAllCommand.run();

        // Verify the service method was called
        verify(astralObjectService).clearGoalMap();
    }

    @Test
    void testDeleteCommand() {
        // Create the command
        DeleteCommand deleteCommand = DeleteCommand.builder()
                .service(astralObjectService)
                .x(4)
                .y(5)
                .build();

        // Execute
        deleteCommand.run();

        // Verify the service was called with the correct parameters
        verify(astralObjectService).processAstralObject(
                any(Polyanet.class),
                eq(HttpMethod.DELETE)
        );
    }
}