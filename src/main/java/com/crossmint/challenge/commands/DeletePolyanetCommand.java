package com.crossmint.challenge.commands;

import com.crossmint.challenge.service.AstralObjectService;
import com.crossmint.challenge.model.Polyanet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


@Component
@Command(name = "delete", description = "Delete a Polyanet at the specified coordinates.")
public class DeletePolyanetCommand implements Runnable {
    @Parameters(index = "0", description = "The x coordinate")
    private int x;
    @Parameters(index = "1", description = "The y coordinate")
    private int y;

    private static final Logger logger = LoggerFactory.getLogger(DeletePolyanetCommand.class);
    private final AstralObjectService service;

    public DeletePolyanetCommand(AstralObjectService service) {
        this.service = service;
    }

    @Override
    public void run() {
        logger.info("Deleting Polyanet at ({}, {})...", x, y);
        service.createObject(new Polyanet(x, y), HttpMethod.DELETE);
    }
}
