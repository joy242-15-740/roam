package com.roam.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {

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
                        System.out.println("🔧 Initializing Hibernate EntityManagerFactory...");
                        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                        System.out.println("✓ Hibernate initialized successfully");
                    } catch (Exception e) {
                        System.err.println("✗ Failed to initialize Hibernate: " + e.getMessage());
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
            System.out.println("🔒 Shutting down Hibernate...");
            entityManagerFactory.close();
            System.out.println("✓ Hibernate shutdown complete");
        }
    }
}
