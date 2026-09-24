package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private final Properties properties = new Properties();

    public Config(String configFile) {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(configFile)) {

            if (input == null) {
                throw new IllegalArgumentException(
                        "Configuration file not found: " + configFile
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load configuration file: " + configFile,
                    e
            );
        }
    }

    public String getBpoDomain() {
        return properties.getProperty("BPO_DOMAIN");
    }

    public String getBpoPort() {
        return properties.getProperty("BPO_PORT");
    }

    public String getBpoContextRoot() {
        return properties.getProperty("BPO_CONTEXT_ROOT");
    }

    public String getNodeId() {
        return properties.getProperty("NODE_ID");
    }

    public String getNextNodeId() {
        return properties.getProperty("NEXT_NODE_ID");
    }

    public String getOverSleeperNode() {
        return properties.getProperty("OVER_SLEEPER_NODE");
    }

    public int getOverSleeperMinimumInSeconds() {
        return Integer.parseInt(
                properties.getProperty("OVERSLEEPER_MINIMUM_IN_SECONDS")
        );
    }
}