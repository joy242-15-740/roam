package com.roam.service;

import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;

public class DatabaseService {

    /**
     * Initialize database connection and schema
     */
    public static void initializeDatabase() {
        try {
            System.out.println("📦 Initializing database...");

            // This will trigger EntityManagerFactory creation
            // which will create the database file and tables
            HibernateUtil.getEntityManagerFactory();

            // Test connection
            if (testConnection()) {
                System.out.println("✓ Database initialized successfully");
                System.out.println("📍 Database location: " + getDatabasePath());
            } else {
                System.err.println("✗ Database connection test failed");
            }

        } catch (Exception e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
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
            System.err.println("Connection test failed: " + e.getMessage());
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
