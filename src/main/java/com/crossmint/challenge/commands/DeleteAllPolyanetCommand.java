package com.crossmint.challenge.commands;

import com.crossmint.challenge.config.GoalMap;
import com.crossmint.challenge.service.AstralObjectService;
import com.crossmint.challenge.model.Polyanet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;


@Component
@Command(name = "deleteall", description = "Delete all Polyanets on the map.")
public class DeleteAllPolyanetCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllPolyanetCommand.class);
    private final int rows, cols;
    private final AstralObjectService service;

    public DeleteAllPolyanetCommand(AstralObjectService service, GoalMap goalMap) {
        this.service = service;
        this.rows = goalMap.rows();
        this.cols = goalMap.cols();
    }

    @Override
    public void run() {
        logger.info("Deleting all Polyanets...");

        int maxParallelism = 3;  // Set a limit on parallel executions
        ForkJoinPool customThreadPool = new ForkJoinPool(maxParallelism);

        try {
            customThreadPool.submit(() ->
                    IntStream.range(0, rows)
                            .boxed()
                            .flatMap(x -> IntStream.range(0, cols)
                                    .mapToObj(y -> new Polyanet(x, y)))  // Create Polyanet objects
                            .parallel()
                            .forEach(polyanet -> {
                                logger.info("Deleting Polyanet at ({}, {})...", polyanet.getRow(), polyanet.getColumn());
                                service.createObject(polyanet, HttpMethod.DELETE);
                            })
            ).get();  // Blocks until execution completes
        } catch (Exception e) {
            logger.error("Error executing deletion tasks", e);
        } finally {
            customThreadPool.shutdown();
        }

        logger.info("All Polyanets deleted.");
    }

}
