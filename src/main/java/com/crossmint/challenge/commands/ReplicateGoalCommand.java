package com.crossmint.challenge.commands;

import com.crossmint.challenge.service.AstralObjectService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "replicategoal", description = "Create all astral objects on the map.")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplicateGoalCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReplicateGoalCommand.class);
    @Autowired
    private AstralObjectService service;

    @Override
    public void run() {
        logger.info("Replicating goal map...");
        service.replicateGoalMap();
        logger.info("Goal map replicated.");
    }

}
