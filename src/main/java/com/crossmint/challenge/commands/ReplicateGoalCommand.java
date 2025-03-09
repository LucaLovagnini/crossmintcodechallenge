package com.crossmint.challenge.commands;

import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "replicategoal", description = "Create all astral objects on the map.")
public class ReplicateGoalCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReplicateGoalCommand.class);
    private final AstralObjectService service;

    public ReplicateGoalCommand(AstralObjectService service) {
        this.service = service;
    }

    @Override
    public void run() {
        logger.info("Replicating goal map...");
        service.replicateGoalMap();
        logger.info("Goal map replicated.");
    }

}
