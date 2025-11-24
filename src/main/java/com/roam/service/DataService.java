package com.roam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.*;
import com.roam.repository.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for exporting and importing all application data
 */
public class DataService {

    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final RegionRepository regionRepository;
    private final CalendarEventRepository eventRepository;
    private final CalendarSourceRepository sourceRepository;
    private final WikiRepository WikiRepository;
    private final WikiTemplateRepository WikiTemplateRepository;
    private final JournalEntryRepository journalRepository;
    private final JournalTemplateRepository journalTemplateRepository;

    private final ObjectMapper objectMapper;

    public DataService() {
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.regionRepository = new RegionRepository();
        this.eventRepository = new CalendarEventRepository();
        this.sourceRepository = new CalendarSourceRepository();
        this.WikiRepository = new WikiRepository();
        this.WikiTemplateRepository = new WikiTemplateRepository();
        this.journalRepository = new JournalEntryRepository();
        this.journalTemplateRepository = new JournalTemplateRepository();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Export all data to JSON file
     */
    public ExportResult exportData(File exportFile) {
        try {
            Map<String, Object> exportData = new HashMap<>();

            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metadata.put("applicationVersion", "1.0.0");
            metadata.put("dataFormat", "json");
            exportData.put("metadata", metadata);

            // Export all entities
            exportData.put("regions", regionRepository.findAll());
            exportData.put("operations", operationRepository.findAll());
            exportData.put("tasks", taskRepository.findAll());
            exportData.put("calendarSources", sourceRepository.findAll());
            exportData.put("calendarEvents", eventRepository.findAll());
            exportData.put("notes", WikiRepository.findAll());
            exportData.put("noteTemplates", WikiTemplateRepository.findCustom());
            exportData.put("journalEntries", journalRepository.findAll());
            exportData.put("journalTemplates", journalTemplateRepository.findAll());

            // Write to file
            objectMapper.writeValue(exportFile, exportData);

            // Calculate counts
            int totalRecords = calculateTotalRecords(exportData);

            return new ExportResult(true, "Data exported successfully", totalRecords, exportFile.getAbsolutePath());

        } catch (IOException e) {
            return new ExportResult(false, "Export failed: " + e.getMessage(), 0, null);
        }
    }

    /**
     * Import data from JSON file
     */
    public ImportResult importData(File importFile, boolean mergeMode) {
        try {
            // Read JSON file
            @SuppressWarnings("unchecked")
            Map<String, Object> importData = objectMapper.readValue(importFile, Map.class);

            int importedCount = 0;
            int skippedCount = 0;
            StringBuilder errors = new StringBuilder();

            // Validate metadata
            if (!importData.containsKey("metadata")) {
                return new ImportResult(false, "Invalid import file: missing metadata", 0, 0);
            }

            // Import regions first (no dependencies)
            if (importData.containsKey("regions")) {
                try {
                    List<Region> regions = objectMapper.convertValue(importData.get("regions"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Region.class));
                    for (Region region : regions) {
                        if (mergeMode && regionExists(region)) {
                            skippedCount++;
                        } else {
                            region.setId(null); // Clear ID to create new
                            regionRepository.save(region);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Regions import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import operations (depends on regions)
            if (importData.containsKey("operations")) {
                try {
                    List<Operation> operations = objectMapper.convertValue(importData.get("operations"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Operation.class));
                    for (Operation operation : operations) {
                        if (mergeMode && operationExists(operation)) {
                            skippedCount++;
                        } else {
                            operation.setId(null);
                            operationRepository.save(operation);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Operations import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import tasks (depends on operations)
            if (importData.containsKey("tasks")) {
                try {
                    List<Task> tasks = objectMapper.convertValue(importData.get("tasks"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                    for (Task task : tasks) {
                        task.setId(null);
                        taskRepository.save(task);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Tasks import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import calendar sources
            if (importData.containsKey("calendarSources")) {
                try {
                    List<CalendarSource> sources = objectMapper.convertValue(importData.get("calendarSources"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, CalendarSource.class));
                    for (CalendarSource source : sources) {
                        if (mergeMode && sourceExists(source)) {
                            skippedCount++;
                        } else {
                            source.setId(null);
                            sourceRepository.save(source);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Calendar sources import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import calendar events
            if (importData.containsKey("calendarEvents")) {
                try {
                    List<CalendarEvent> events = objectMapper.convertValue(importData.get("calendarEvents"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, CalendarEvent.class));
                    for (CalendarEvent event : events) {
                        event.setId(null);
                        eventRepository.save(event);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Calendar events import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import notes
            if (importData.containsKey("notes")) {
                try {
                    List<Wiki> notes = objectMapper.convertValue(importData.get("notes"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Wiki.class));
                    for (Wiki Wiki : notes) {
                        Wiki.setId(null);
                        WikiRepository.save(Wiki);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Notes import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import Wiki templates
            if (importData.containsKey("noteTemplates")) {
                try {
                    List<WikiTemplate> templates = objectMapper.convertValue(importData.get("noteTemplates"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, WikiTemplate.class));
                    for (WikiTemplate template : templates) {
                        if (mergeMode && noteTemplateExists(template)) {
                            skippedCount++;
                        } else {
                            template.setId(null);
                            WikiTemplateRepository.save(template);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Wiki templates import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import journal entries
            if (importData.containsKey("journalEntries")) {
                try {
                    List<JournalEntry> entries = objectMapper.convertValue(importData.get("journalEntries"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, JournalEntry.class));
                    for (JournalEntry entry : entries) {
                        entry.setId(null);
                        journalRepository.save(entry);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Journal entries import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import journal templates
            if (importData.containsKey("journalTemplates")) {
                try {
                    List<JournalTemplate> templates = objectMapper.convertValue(importData.get("journalTemplates"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, JournalTemplate.class));
                    for (JournalTemplate template : templates) {
                        template.setId(null);
                        journalTemplateRepository.save(template);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Journal templates import error: ").append(e.getMessage()).append("\n");
                }
            }

            String message = errors.length() > 0
                    ? "Import completed with errors:\n" + errors.toString()
                    : "Data imported successfully";

            return new ImportResult(true, message, importedCount, skippedCount);

        } catch (IOException e) {
            return new ImportResult(false, "Import failed: " + e.getMessage(), 0, 0);
        }
    }

    // Helper methods to check for existing entities
    private boolean regionExists(Region region) {
        return regionRepository.findAll().stream()
                .anyMatch(r -> r.getName().equals(region.getName()));
    }

    private boolean operationExists(Operation operation) {
        return operationRepository.findAll().stream()
                .anyMatch(o -> o.getName().equals(operation.getName()));
    }

    private boolean sourceExists(CalendarSource source) {
        return sourceRepository.findAll().stream()
                .anyMatch(s -> s.getName().equals(source.getName()));
    }

    private boolean noteTemplateExists(WikiTemplate template) {
        return WikiTemplateRepository.findAll().stream()
                .anyMatch(t -> t.getName().equals(template.getName()));
    }

    private int calculateTotalRecords(Map<String, Object> data) {
        int total = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof List) {
                total += ((List<?>) entry.getValue()).size();
            }
        }
        return total;
    }

    // Result classes
    public static class ExportResult {
        private final boolean success;
        private final String message;
        private final int recordCount;
        private final String filePath;

        public ExportResult(boolean success, String message, int recordCount, String filePath) {
            this.success = success;
            this.message = message;
            this.recordCount = recordCount;
            this.filePath = filePath;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    public static class ImportResult {
        private final boolean success;
        private final String message;
        private final int importedCount;
        private final int skippedCount;

        public ImportResult(boolean success, String message, int importedCount, int skippedCount) {
            this.success = success;
            this.message = message;
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getImportedCount() {
            return importedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }
    }
}
