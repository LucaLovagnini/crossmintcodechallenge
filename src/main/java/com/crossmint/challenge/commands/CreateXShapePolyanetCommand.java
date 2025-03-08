package com.crossmint.challenge.commands;

import com.crossmint.challenge.config.GoalMap;
import com.crossmint.challenge.service.AstralObjectService;
import com.crossmint.challenge.model.Polyanet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

@Component
@Command(name = "xshape", description = "Creates a xshape with an optional starting point.")
public class CreateXShapePolyanetCommand implements Runnable {
    @Parameters(index = "0", description = "Starting point of the X-shape", defaultValue = "0", arity = "0..1")
    private int start;

    private static final Logger logger = LoggerFactory.getLogger(CreateXShapePolyanetCommand.class);
    private final int rows;
    private final AstralObjectService service;

    public CreateXShapePolyanetCommand(AstralObjectService service, GoalMap goalMap) {
        this.service = service;
        this.rows = goalMap.rows();
        //ToDo: check that the map is square and start makes sense
    }

    @Override
    public void run() {
        logger.info("Creating X shape Polyanet starting from {}", start);
        int maxParallelism = 3;  // Set a limit on parallel executions
        ForkJoinPool customThreadPool = new ForkJoinPool(maxParallelism);

        try {
            IntStream.range(start, rows-start)
                    .boxed()
                    .map(x -> Map.entry(new Polyanet(x, x),
                                    new Polyanet(x, rows - x - 1)))
                    .forEach(pair -> {
                        logger.info("Creating Planet at ({}, {}) and its opposite at ({}, {})...",
                                pair.getKey().getRow(), pair.getKey().getColumn(),
                                pair.getValue().getRow(), pair.getValue().getColumn());
                        service.createObject(pair.getKey(), HttpMethod.POST);
                        service.createObject(pair.getValue(), HttpMethod.POST);
                    });
        } catch (Exception e) {
            logger.error("Error executing deletion tasks", e);
        } finally {
            customThreadPool.shutdown();
        }
    }

}

