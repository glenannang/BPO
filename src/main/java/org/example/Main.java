package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        Config config =
                new Config("node4.properties");

        SleepProcessor processor =
                new SleepProcessor(config);

        /*
         * NODE4 has two assigned workers:
         *
         * Sleeper4-1
         * Sleeper4-2
         */
        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        executor.submit(
                () -> processor.processWorker("Sleeper4-1")
        );

        executor.submit(
                () -> processor.processWorker("Sleeper4-2")
        );

        executor.shutdown();
    }
}