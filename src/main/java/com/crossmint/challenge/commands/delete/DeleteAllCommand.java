package com.crossmint.challenge.commands.delete;

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
@Command(name = "deleteall", description = "Delete all astral objects on the map.")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class DeleteAllCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllCommand.class);
    @Autowired
    private final AstralObjectService service;

    @Override
    public void run() {
        logger.info("Deleting all astral objects...");
        service.clearGoalMap();
        logger.info("All astral objects deleted.");
    }

}
