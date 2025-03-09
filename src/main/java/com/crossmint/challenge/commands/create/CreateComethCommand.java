package com.crossmint.challenge.commands.create;

import com.crossmint.challenge.commands.ProcessAstralObjectCommand;
import com.crossmint.challenge.model.Cometh;
import com.crossmint.challenge.model.ComethDirection;
import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "cometh", description = "Creates a Cometh at the specified coordinates.", mixinStandardHelpOptions = true)
public class CreateComethCommand extends ProcessAstralObjectCommand {
    private static final Logger logger = LoggerFactory.getLogger(CreateComethCommand.class);

    @Parameters(index = "2", description = "The direction of the Cometh (UP, DOWN, LEFT, RIGHT)")
    private ComethDirection direction;

    public CreateComethCommand(AstralObjectService service) {
        super(service);
    }

    @Override
    public void run() {
        logger.info("Creating Cometh facing {} at ({}, {})...", direction, x, y);
        processAstralObject(new Cometh(x, y, direction), HttpMethod.POST);
    }
}
