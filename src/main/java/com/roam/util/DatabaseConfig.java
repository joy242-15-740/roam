package com.roam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Database configuration manager that loads credentials from environment
 * variables
 * or a properties file outside version control.
 * 
 * Priority order:
 * 1. Environment variables (DB_USER, DB_PASSWORD)
 * 2. User home config file (~/.roam/database.properties)
 * 3. Fallback defaults (for development only)
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_DIR = ".roam";
    private static final String CONFIG_FILE = "database.properties";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String driver;

    private static DatabaseConfig instance;

    private DatabaseConfig() {
        // Try environment variables first (highest priority)
        String envUser = System.getenv("ROAM_DB_USER");
        String envPassword = System.getenv("ROAM_DB_PASSWORD");
        String envUrl = System.getenv("ROAM_DB_URL");
        String envDriver = System.getenv("ROAM_DB_DRIVER");

        if (envUser != null && envPassword != null) {
            this.username = envUser;
            this.password = envPassword;
            this.jdbcUrl = envUrl != null ? envUrl : getDefaultJdbcUrl();
            this.driver = envDriver != null ? envDriver : "org.h2.Driver";
            logger.info("✓ Database configuration loaded from environment variables");
        } else {
            // Try loading from properties file
            Properties props = loadPropertiesFile();
            if (props != null && props.containsKey("db.username")) {
                this.username = props.getProperty("db.username");
                this.password = props.getProperty("db.password");
                this.jdbcUrl = props.getProperty("db.url", getDefaultJdbcUrl());
                this.driver = props.getProperty("db.driver", "org.h2.Driver");
                logger.info("✓ Database configuration loaded from properties file");
            } else {
                // Fallback to defaults (development only - maintains backward compatibility)
                logger.warn("⚠️  WARNING: Using default database credentials!");
                logger.warn("⚠️  Set ROAM_DB_USER and ROAM_DB_PASSWORD environment variables for production");
                this.username = "admin";
                // Use legacy password for backward compatibility with existing databases
                // Change this in production by setting environment variables
                this.password = "hulululu";
                this.jdbcUrl = getDefaultJdbcUrl();
                this.driver = "org.h2.Driver";
            }
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private Properties loadPropertiesFile() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            props.load(input);
            return props;
        } catch (IOException e) {
            logger.error("Failed to load database properties: {}", e.getMessage());
            return null;
        }
    }

    private Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }

    private String getDefaultJdbcUrl() {
        String userHome = System.getProperty("user.home");
        return String.format(
                "jdbc:h2:file:%s/roam/roamdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                userHome.replace("\\", "/"));
    }

    private String generateRandomPassword() {
        // Generate a secure random password for development
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }

    /**
     * Creates a sample configuration file in user's home directory
     */
    public static void createSampleConfigFile() {
        try {
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, CONFIG_DIR);
            Path configFile = configDir.resolve(CONFIG_FILE);

            if (Files.exists(configFile)) {
                logger.info("Configuration file already exists: {}", configFile);
                return;
            }

            Files.createDirectories(configDir);

            String sampleConfig = "# Roam Database Configuration\n" +
                    "# DO NOT commit this file to version control!\n" +
                    "\n" +
                    "# Database connection settings\n" +
                    "db.driver=org.h2.Driver\n" +
                    "db.url=jdbc:h2:file:~/roam/roamdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE\n" +
                    "db.username=admin\n" +
                    "db.password=CHANGE_ME_TO_SECURE_PASSWORD\n" +
                    "\n" +
                    "# For PostgreSQL (production):\n" +
                    "# db.driver=org.postgresql.Driver\n" +
                    "# db.url=jdbc:postgresql://localhost:5432/roamdb\n" +
                    "# db.username=roam_user\n" +
                    "# db.password=your_secure_password\n";

            Files.write(configFile, sampleConfig.getBytes());
            logger.info("✓ Sample configuration file created: {}", configFile);
            logger.warn("⚠️  Please update the password in this file!");

        } catch (IOException e) {
            logger.error("Failed to create configuration file: {}", e.getMessage());
        }
    }
}
