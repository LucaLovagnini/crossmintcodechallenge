package com.crossmint.challenge.commands;

import picocli.CommandLine.Command;
import org.springframework.stereotype.Component;

@Component
@Command(name = "crossmint", mixinStandardHelpOptions = true, subcommands = {
        CreatePolyanetCommand.class,
        DeletePolyanetCommand.class,
        DeleteAllPolyanetCommand.class,
        CreateXShapePolyanetCommand.class
})
public class MainCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use one of the available commands: create, delete, deleteAll, xshape.");
    }
}
