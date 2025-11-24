package com.roam.service;

import com.roam.repository.WikiTemplateRepository;
import com.roam.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataInitializer {

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        private final WikiTemplateRepository templateRepository;
        private final RegionRepository regionRepository;

        public DataInitializer() {
                this.templateRepository = new WikiTemplateRepository();
                this.regionRepository = new RegionRepository();
        }

        public void initializeDefaultTemplates() {
                // Initialize regions first
                initializeDefaultRegions();

                // Check if templates already exist
                if (!templateRepository.findAll().isEmpty()) {
                        logger.info("✓ Templates already initialized");
                        return;
                }

                // Default templates creation removed as per user request
                /*
                 * logger.info("📝 Creating default Wiki templates...");
                 * 
                 * // Template 1: Blank Wiki
                 * WikiTemplate blank = new WikiTemplate(
                 * "Blank Wiki",
                 * "Start with a blank canvas",
                 * "",
                 * "📄",
                 * true);
                 * templateRepository.save(blank);
                 * 
                 * // ... (other templates) ...
                 * 
                 * logger.info("✓ Default templates created successfully");
                 */
        }

        private void initializeDefaultRegions() {
                logger.info("🌍 Creating default regions...");
                regionRepository.createDefaultRegions();
                logger.info("✓ Default regions created successfully");
        }
}