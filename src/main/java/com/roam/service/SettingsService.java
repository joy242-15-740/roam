package com.roam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    private static SettingsService instance;
    private static final String SETTINGS_FILE = "settings.json";
    private Settings settings;
    private final ObjectMapper mapper;

    private SettingsService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadSettings();
    }

    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() {
        try {
            mapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            logger.error("Failed to save settings: {}", e.getMessage());
        }
    }

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                settings = mapper.readValue(file, Settings.class);
            } catch (IOException e) {
                logger.error("Failed to load settings, using defaults: {}", e.getMessage());
                settings = new Settings();
            }
        } else {
            settings = new Settings();
            saveSettings();
        }
    }
}
