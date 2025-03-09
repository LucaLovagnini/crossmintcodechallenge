package com.crossmint.challenge.commands.delete;

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
@Command(name = "delete", description = "Delete astral object at the specified coordinates.", mixinStandardHelpOptions = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteCommand extends ProcessAstralObjectCommand {
    private static final Logger logger = LoggerFactory.getLogger(DeleteCommand.class);

    @Override
    public void run() {
        logger.info("Deleting astral object at ({}, {})...", x, y);
        processAstralObject(new Polyanet(x, y), HttpMethod.DELETE);
    }
}
