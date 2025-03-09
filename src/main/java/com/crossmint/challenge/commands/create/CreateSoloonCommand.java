package com.crossmint.challenge.commands.create;

import com.crossmint.challenge.commands.ProcessAstralObjectCommand;
import com.crossmint.challenge.model.Soloon;
import com.crossmint.challenge.model.SoloonColor;
import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "soloon", description = "Creates a Soloon at the specified coordinates.", mixinStandardHelpOptions = true)
public class CreateSoloonCommand extends ProcessAstralObjectCommand {
    private static final Logger logger = LoggerFactory.getLogger(CreateSoloonCommand.class);

    @Parameters(index = "2", description = "The color of the Soloon (BLUE, RED, PURPLE, WHITE)")
    private SoloonColor color;

    public CreateSoloonCommand(AstralObjectService service) {
        super(service);
    }

    @Override
    public void run() {
        logger.info("Creating {} Soloon at ({}, {})...", color, x, y);
        processAstralObject(new Soloon(x, y, color), HttpMethod.POST);
    }
}
