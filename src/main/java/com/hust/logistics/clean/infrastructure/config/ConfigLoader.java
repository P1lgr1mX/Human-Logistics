package com.hust.logistics.clean.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public AppConfig loadFromResource(String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot find resource: " + resourceName);
            }
            AppConfig config = MAPPER.readValue(inputStream, AppConfig.class);
            validate(config);
            return config;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse config resource: " + resourceName, exception);
        }
    }

    public AppConfig loadFromPath(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            AppConfig config = MAPPER.readValue(inputStream, AppConfig.class);
            validate(config);
            return config;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse config path: " + path, exception);
        }
    }

    private void validate(AppConfig config) {
        if (config.getStartTime() != null && config.getEndTime() != null
                && config.getStartTime().isAfter(config.getEndTime())) {
            throw new IllegalArgumentException("Invalid time range: startTime must be before or equal to endTime.");
        }
    }
}
