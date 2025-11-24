package com.roam.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static volatile EntityManagerFactory entityManagerFactory;
    private static final String PERSISTENCE_UNIT_NAME = "roam-pu";

    // Private constructor to prevent instantiation
    private HibernateUtil() {
    }

    /**
     * Get EntityManagerFactory instance (Singleton, thread-safe)
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (HibernateUtil.class) {
                if (entityManagerFactory == null) {
                    try {
                        logger.info("🔧 Initializing Hibernate EntityManagerFactory...");

                        // Load database configuration dynamically
                        DatabaseConfig dbConfig = DatabaseConfig.getInstance();

                        // ===== FLYWAY MIGRATIONS =====
                        // Run database migrations BEFORE initializing Hibernate
                        logger.info("🔄 Running Flyway database migrations...");
                        boolean migrationSuccess = FlywayManager.runMigrations(
                                dbConfig.getJdbcUrl(),
                                dbConfig.getUsername(),
                                dbConfig.getPassword());

                        if (!migrationSuccess) {
                            throw new RuntimeException("Database migration failed. Cannot initialize Hibernate.");
                        }

                        // Override persistence.xml properties with runtime values
                        Map<String, String> properties = new HashMap<>();
                        properties.put("jakarta.persistence.jdbc.driver", dbConfig.getDriver());
                        properties.put("jakarta.persistence.jdbc.url", dbConfig.getJdbcUrl());
                        properties.put("jakarta.persistence.jdbc.user", dbConfig.getUsername());
                        properties.put("jakarta.persistence.jdbc.password", dbConfig.getPassword());

                        entityManagerFactory = Persistence.createEntityManagerFactory(
                                PERSISTENCE_UNIT_NAME,
                                properties);
                        logger.info("✓ Hibernate initialized successfully");
                    } catch (Exception e) {
                        logger.error("✗ Failed to initialize Hibernate: {}", e.getMessage(), e);
                        e.printStackTrace();
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return entityManagerFactory;
    }

    /**
     * Get a new EntityManager instance
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Shutdown Hibernate (call on application exit)
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            logger.info("🔒 Shutting down Hibernate...");
            entityManagerFactory.close();
            logger.info("✓ Hibernate shutdown complete");
        }
    }
}
