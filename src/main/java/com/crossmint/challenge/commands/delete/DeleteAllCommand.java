package com.crossmint.challenge.commands.delete;

import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;


@Component
@Command(name = "deleteall", description = "Delete all astral objects on the map.")
public class DeleteAllCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllCommand.class);
    private final AstralObjectService service;

    public DeleteAllCommand(AstralObjectService service) {
        this.service = service;
    }

    @Override
    public void run() {
        logger.info("Deleting all astral objects...");
        service.clearGoalMap();
        logger.info("All astral objects deleted.");
    }

}
