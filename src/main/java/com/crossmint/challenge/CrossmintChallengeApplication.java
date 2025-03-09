package com.crossmint.challenge;

import com.crossmint.challenge.commands.MainCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class CrossmintChallengeApplication implements CommandLineRunner {

    @Autowired
    private IFactory picocliFactory;

    @Autowired
    private MainCommand mainCommand;

    public static void main(String[] args) {
        SpringApplication.run(CrossmintChallengeApplication.class, args);
    }

    @Override
    public void run(String... args) {
        int exitCode = new CommandLine(mainCommand, picocliFactory).execute(args);
        System.exit(exitCode);
    }
}
