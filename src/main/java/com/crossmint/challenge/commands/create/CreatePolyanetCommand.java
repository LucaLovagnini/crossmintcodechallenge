package com.crossmint.challenge.commands.create;

import com.crossmint.challenge.commands.ProcessAstralObjectCommand;
import com.crossmint.challenge.model.Polyanet;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "polyanet", description = "Creates a Polyanet at the specified coordinates.")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreatePolyanetCommand extends ProcessAstralObjectCommand {
    private static final Logger logger = LoggerFactory.getLogger(CreatePolyanetCommand.class);

    @Override
    public void run() {
        logger.info("Creating Polyanet at ({}, {})...", x, y);
        processAstralObject(new Polyanet(x, y), HttpMethod.POST);
    }
}
