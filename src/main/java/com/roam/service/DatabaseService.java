package com.roam.service;

import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    /**
     * Initialize database connection and schema
     */
    public static void initializeDatabase() {
        try {
            logger.info("📦 Initializing database...");

            // This will trigger EntityManagerFactory creation
            // which will create the database file and tables
            HibernateUtil.getEntityManagerFactory();

            // Test connection
            if (testConnection()) {
                logger.info("✓ Database initialized successfully");
                logger.info("📍 Database location: {}", getDatabasePath());

                // Initialize default templates
                DataInitializer initializer = new DataInitializer();
                initializer.initializeDefaultTemplates();
            } else {
                logger.error("✗ Database connection test failed");
            }

        } catch (Exception e) {
            logger.error("✗ Database initialization failed: {}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        EntityManager em = null;
        try {
            em = HibernateUtil.getEntityManager();
            // Simple query to test connection
            em.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Get database file path
     */
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        return userHome + "/roam/roamdb.mv.db";
    }
}
