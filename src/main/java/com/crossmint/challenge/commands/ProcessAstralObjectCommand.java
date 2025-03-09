package com.crossmint.challenge.commands;

import com.crossmint.challenge.model.ApiSerializable;
import com.crossmint.challenge.model.GoalMap;
import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


@Command(mixinStandardHelpOptions = true)
public abstract class ProcessAstralObjectCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessAstralObjectCommand.class);

    @Parameters(index = "0", description = "The x coordinate")
    protected int x;

    @Parameters(index = "1", description = "The y coordinate")
    protected int y;

    private final AstralObjectService service;

    public ProcessAstralObjectCommand(AstralObjectService service) {
        this.service = service;
    }

    protected void processAstralObject(ApiSerializable astralObject, HttpMethod method) {
        GoalMap goalMap = this.service.getGoalMap();
        int rows = goalMap.rows(), cols = goalMap.cols();
        if(x < 0 || y < 0 || x >= rows || y >= cols) {
            logger.error("Invalid coordinates ({}, {}) for map with {} rows and {} cols", x, y, rows, cols);
            return;
        }
        service.processAstralObject(astralObject, method);
    }


}
