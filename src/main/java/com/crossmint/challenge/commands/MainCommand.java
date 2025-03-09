package com.crossmint.challenge.commands;

import com.crossmint.challenge.commands.create.CreateCommand;
import com.crossmint.challenge.commands.delete.DeleteAllCommand;
import com.crossmint.challenge.commands.delete.DeleteCommand;
import picocli.CommandLine.Command;
import org.springframework.stereotype.Component;

@Component
@Command(name = "crossmint", mixinStandardHelpOptions = true, subcommands = {
        CreateCommand.class,
        DeleteCommand.class,
        DeleteAllCommand.class,
        ReplicateGoalCommand.class
})
public class MainCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use one of the available commands: create, delete, deleteAll, replicategoal.");
    }
}
