package de.getit.devops;

import de.getit.devops.jmx.JmxProxy;
import de.getit.devops.jmx.Resource;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.Instant;
import org.joda.time.Interval;

import javax.management.JMException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckTomcat
 * <p>
 * Wait, until all applications in a tomcat servlet container are started.
 * Uses JMX to check this.
 */
public class CheckTomcat {

    private static Options options;
    public static boolean debugMode;

    private static void showHelp(final String errorMessage) {

        final String osNewLines = System.lineSeparator() +
            System.lineSeparator();

        String header = "";

        if (StringUtils.isNotBlank(errorMessage)) {
            header =
                String.format(
                    "%s%s",
                    errorMessage,
                    osNewLines
                );
        }

        header =
            String.format(
                "%sWait until all applications in a tomcat servlet container" +
                    " are started. Will return exit code 3, if the jmx port " +
                    "can not be reached, 2 if the timeout has been reached " +
                    "and 1 on other errors.%s",
                header,
                osNewLines
            );

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("check-tomcat", header, CheckTomcat.options, "",
            true);

        if (StringUtils.isNotBlank(errorMessage)) {
            System.exit(1);
        } else {
            System.exit(0);
        }

    }

    public static void main(final String[] args) throws Exception {

        CheckTomcat.options = new Options();

        options.addOption(Option.builder("j")
            .longOpt("jmx")
            .hasArg()
            .desc("JMX-URL (like " +
                "service:jmx:rmi:///jndi/rmi://myjmxhost:myjmxport/jmxrmi)")
            .required()
            .build()
        );

        options.addOption(Option.builder("u")
            .longOpt("user")
            .hasArg()
            .desc("Username for JMX connection")
            .build()
        );

        options.addOption(Option.builder("p")
            .longOpt("password")
            .hasArg()
            .desc("Password for JMX connection")
            .build()
        );

        options.addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show this help")
            .build()
        );

        options.addOption(Option.builder("t")
            .longOpt("timeout")
            .hasArg()
            .desc("Wait this number of seconds, before canceling")
            .build()
        );

        options.addOption(Option.builder("d")
            .longOpt("debug")
            .desc("Enable debug mode")
            .build()
        );

        options.addOption(Option.builder("r")
            .desc("Resource paths to check")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .argName("HOST:PATH[,HOST:PATH]")
            .required()
            .build()
        );

        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {

            showHelp(e.getMessage());

        }

        if (cmd == null) {
            System.out.println("Commandline can not be parsed.");
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            final String header = "Wait until all applications in a tomcat " +
                "servlet container are started.\n\n";

            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("myapp", header, options, "", true);

            System.exit(0);
        }

        JmxProxy proxy;

        try {

            if (cmd.hasOption("u")) {

                proxy = new JmxProxy(
                    cmd.getOptionValue("j"),
                    cmd.getOptionValue("u"),
                    cmd.getOptionValue("p")
                );

            } else {

                proxy = new JmxProxy(cmd.getOptionValue("j"));

            }
        } catch (JMException e) {

            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                // JMX port available, but maybe after some seconds. Return
                // a special return code for that.

                System.exit(3);
            }

            showHelp(e.getMessage());
            return;

        }

        int timeout = 600;

        if (cmd.hasOption("t")) {

            try {

                timeout = Integer.valueOf(cmd.getOptionValue("t"));

            } catch (final NumberFormatException e) {

                showHelp("Timeout parameter is no number.");

            }

        }

        CheckTomcat.debugMode = false;

        if (cmd.hasOption("d")) {
            CheckTomcat.debugMode = true;
        }

        final List<Resource> resources = new ArrayList<>();

        for (final String value : cmd.getOptionValues("r")) {

            final String[] resourceConfig = value.split(":");

            final Resource resource;

            if (resourceConfig.length == 1) {
                // The path is missing. This is a root resource

                resource = new Resource(
                    resourceConfig[0],
                    "/"
                );

            } else {

                resource = new Resource(
                    resourceConfig[0],
                    "/" + resourceConfig[1]
                );
            }


            resources.add(resource);

        }

        if (CheckTomcat.debugMode) {
            System.err.println(
                String.format(
                    "Found %d ressources.",
                    resources.size()
                )
            );
        }

        final ArrayList<Resource> startedRessources = new ArrayList<>();

        final Instant startedInstant = new Instant();

        while (startedRessources.size() < resources.size()) {

            for (final Resource resource : resources) {

                if (!startedRessources.contains(resource)) {

                    if (CheckTomcat.debugMode) {
                        System.err.println(
                            String.format(
                                "Checking resource %s%s",
                                resource.getHost(),
                                resource.getPath()
                            )
                        );
                    }

                    if (proxy.isResourceStarted(resource)) {

                        if (CheckTomcat.debugMode) {
                            System.err.println(
                                "Resource started."
                            );
                        }

                        startedRessources.add(resource);

                    } else {

                        if (CheckTomcat.debugMode) {
                            System.err.println(
                                "Resource not yet started."
                            );
                        }

                    }

                }

            }

            final Instant now = new Instant();

            if (new Interval(startedInstant, now).toDuration()
                .getStandardSeconds() > timeout) {

                System.out.println("Timeout reached!");
                System.exit(2);

            }


        }

    }

}

