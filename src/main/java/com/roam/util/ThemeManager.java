package com.roam.util;

import atlantafx.base.theme.*;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThemeManager {

    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    private static ThemeManager instance;
    private String currentTheme = "Light";

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void applyTheme(String themeName) {
        this.currentTheme = themeName;

        switch (themeName.toLowerCase()) {
            case "dark":
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                break;
            case "nord light":
                Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                break;
            case "nord dark":
                Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                break;
            case "cupertino light":
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                break;
            case "cupertino dark":
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                break;
            case "dracula":
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                break;
            case "light":
            default:
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                break;
        }

        logger.info("✓ Applied AtlantaFX theme: {}", themeName);
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkTheme() {
        return currentTheme.toLowerCase().contains("dark") ||
                currentTheme.equalsIgnoreCase("dracula");
    }
}
