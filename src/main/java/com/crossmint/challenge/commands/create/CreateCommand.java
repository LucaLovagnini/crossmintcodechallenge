package com.crossmint.challenge.commands.create;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "create",
        description = "Creates an Astral Object (Polyanet, Soloon, or Cometh).",
        subcommands = {
                CreatePolyanetCommand.class,
                CreateSoloonCommand.class,
                CreateComethCommand.class
        }
)
public class CreateCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Usage: create <polyanet|soloon|cometh> [options]");
    }
}
