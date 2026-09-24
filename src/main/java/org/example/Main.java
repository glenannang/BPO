package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println(
                    "Please provide a configuration file."
            );
            System.err.println(
                    "Example: java -jar BPO.jar node1.properties"
            );
            return;
        }

        String configFile = args[0];

        Config config = new Config(configFile);

        SleepProcessor processor =
                new SleepProcessor(config);

        String[] workers =
                getWorkers(config.getNodeId());

        ExecutorService executor =
                Executors.newFixedThreadPool(workers.length);

        for (String worker : workers) {
            executor.submit(
                    () -> processor.processWorker(worker)
            );
        }

        executor.shutdown();
    }

    private static String[] getWorkers(String nodeId) {

        return switch (nodeId.toUpperCase()) {

            case "NODE1" ->
                    new String[]{
                            "Sleeper1-1",
                            "Sleeper1-2"
                    };

            case "NODE2" ->
                    new String[]{
                            "Sleeper2-1",
                            "Sleeper2-2",
                            "Sleeper2-3"
                    };

            case "NODE3" ->
                    new String[]{
                            "Sleeper3-1",
                            "Sleeper3-2"
                    };

            case "NODE4" ->
                    new String[]{
                            "Sleeper4-1",
                            "Sleeper4-2"
                    };

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported node: " + nodeId
                    );
        };
    }
}