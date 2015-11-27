package com.skeptomai;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

public class Cli {
    private static final Logger log = Logger.getLogger(Cli.class.getName());
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {

        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("u", "url", true, "URL to gather the mp3 filenames");
        options.addOption("o", "output_dir", true, "output directory for the mp3s");

    }

    public HashMap<String, String> parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        HashMap<String, String> m = new HashMap<String, String>();

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
                return null;
            }

            if (cmd.hasOption("u")) {
                String urlToFetch = cmd.getOptionValue("u");
                log.log(Level.INFO, "Using cli argument -u=" + urlToFetch);
                m.put("u",urlToFetch);
            } else {
                log.log(Level.SEVERE, "Missing -u URL option");
                help();
                return null;
            }

            if (cmd.hasOption("o")) {
                String outputDir = cmd.getOptionValue("o");
                log.info("Outputting to dir " + outputDir);
                m.put("o", outputDir);
            }

        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse command line properties", e);
            help();
            return null;
        }

        return m;
    }

    private void help() {
        // This prints out some help
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("Main", options);
        System.exit(0);
    }
}