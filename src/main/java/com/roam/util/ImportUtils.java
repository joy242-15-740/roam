package com.roam.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.CalendarEvent;
import com.roam.model.JournalEntry;
import com.roam.model.Operation;
import com.roam.model.Wiki;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.JournalEntryRepository;
import com.roam.repository.OperationRepository;
import com.roam.repository.WikiRepository;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ImportUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImportUtils.class);
    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public static List<Wiki> importNotesFromMarkdown(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Wikis");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        List<File> files = fileChooser.showOpenMultipleDialog(owner);
        List<Wiki> importedNotes = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            WikiRepository WikiRepository = new WikiRepository();
            int successCount = 0;
            int errorCount = 0;

            for (File file : files) {
                try {
                    Wiki Wiki = parseMarkdownFile(file);
                    if (Wiki != null) {
                        WikiRepository.save(Wiki);
                        importedNotes.add(Wiki);
                        successCount++;
                    }
                } catch (IOException e) {
                    errorCount++;
                    logger.error("Failed to import: {} - {}", file.getName(), e.getMessage(), e);
                }
            }

            showImportResult(successCount, errorCount, "Wiki(s)");
        }

        return importedNotes;
    }

    public static List<CalendarEvent> importEventsFromICS(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Events");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("iCalendar Files", "*.ics"));

        File file = fileChooser.showOpenDialog(owner);
        List<CalendarEvent> importedEvents = new ArrayList<>();

        if (file != null) {
            CalendarEventRepository eventRepository = new CalendarEventRepository();
            int successCount = 0;
            int errorCount = 0;

            try {
                List<CalendarEvent> events = parseICSFile(file);
                for (CalendarEvent event : events) {
                    eventRepository.save(event);
                    importedEvents.add(event);
                    successCount++;
                }
            } catch (IOException e) {
                errorCount++;
                logger.error("Failed to import events: {} - {}", file.getName(), e.getMessage(), e);
            }

            showImportResult(successCount, errorCount, "Event(s)");
        }

        return importedEvents;
    }

    public static List<Operation> importOperationsFromJSON(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Operations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(owner);
        List<Operation> importedOperations = new ArrayList<>();

        if (file != null) {
            OperationRepository operationRepository = new OperationRepository();
            int successCount = 0;
            int errorCount = 0;

            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<Operation> operations = mapper.readValue(file, new TypeReference<List<Operation>>() {
                });

                for (Operation op : operations) {
                    // Reset ID to allow new insertion
                    op.setId(null);
                    operationRepository.save(op);
                    importedOperations.add(op);
                    successCount++;
                }
            } catch (IOException e) {
                errorCount++;
                logger.error("Failed to import operations: {} - {}", file.getName(), e.getMessage(), e);
            }

            showImportResult(successCount, errorCount, "Operation(s)");
        }

        return importedOperations;
    }

    public static List<JournalEntry> importJournalsFromMarkdown(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Journals");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        List<File> files = fileChooser.showOpenMultipleDialog(owner);
        List<JournalEntry> importedEntries = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            JournalEntryRepository journalRepository = new JournalEntryRepository();
            int successCount = 0;
            int errorCount = 0;

            for (File file : files) {
                try {
                    JournalEntry entry = parseJournalMarkdownFile(file);
                    if (entry != null) {
                        journalRepository.save(entry);
                        importedEntries.add(entry);
                        successCount++;
                    }
                } catch (IOException e) {
                    errorCount++;
                    logger.error("Failed to import journal: {} - {}", file.getName(), e.getMessage(), e);
                }
            }

            showImportResult(successCount, errorCount, "Journal Entry(s)");
        }

        return importedEntries;
    }

    private static void showImportResult(int successCount, int errorCount, String type) {
        if (successCount > 0) {
            DialogUtils.showSuccess(successCount + " " + type + " imported successfully!" +
                    (errorCount > 0 ? "\n" + errorCount + " file(s) failed to import." : ""));
        } else if (errorCount > 0) {
            DialogUtils.showError("Import Error", "No " + type.toLowerCase() + " imported",
                    "Failed to import files.");
        }
    }

    private static Wiki parseMarkdownFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        String title = null;
        boolean inFrontMatter = false;
        boolean frontMatterProcessed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check for YAML front matter start
                if (line.trim().equals("---")) {
                    if (!frontMatterProcessed) {
                        inFrontMatter = !inFrontMatter;
                        if (!inFrontMatter) {
                            frontMatterProcessed = true;
                        }
                        continue;
                    }
                }

                // Parse front matter
                if (inFrontMatter) {
                    if (line.startsWith("title:")) {
                        title = line.substring(6).trim();
                    }
                    // Could parse more metadata like date, tags, etc.
                    continue;
                }

                // Add to content (skip empty lines after front matter)
                if (frontMatterProcessed || !line.trim().isEmpty()) {
                    content.append(line).append("\n");
                }
            }
        }

        // Use filename as title if not found in front matter
        if (title == null || title.isEmpty()) {
            title = file.getName().replaceAll("\\.md$", "").replaceAll("_", " ");
        }

        // Create Wiki
        Wiki Wiki = new Wiki();
        Wiki.setTitle(title);
        Wiki.setContent(content.toString().trim());
        Wiki.setCreatedAt(LocalDateTime.now());
        Wiki.setUpdatedAt(LocalDateTime.now());
        Wiki.setIsFavorite(false);

        // Calculate word count
        String text = content.toString().replaceAll("[^a-zA-Z\\s]", "");
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        Wiki.setWordCount(wordCount);

        return Wiki;
    }

    private static List<CalendarEvent> parseICSFile(File file) throws IOException {
        List<CalendarEvent> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            CalendarEvent currentEvent = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("BEGIN:VEVENT")) {
                    currentEvent = new CalendarEvent();
                } else if (line.equals("END:VEVENT")) {
                    if (currentEvent != null) {
                        events.add(currentEvent);
                        currentEvent = null;
                    }
                } else if (currentEvent != null) {
                    if (line.startsWith("SUMMARY:")) {
                        currentEvent.setTitle(line.substring(8));
                    } else if (line.startsWith("DESCRIPTION:")) {
                        currentEvent.setDescription(line.substring(12).replace("\\n", "\n"));
                    } else if (line.startsWith("LOCATION:")) {
                        currentEvent.setLocation(line.substring(9));
                    } else if (line.startsWith("DTSTART:")) {
                        currentEvent.setStartDateTime(LocalDateTime.parse(line.substring(8), ICS_DATE_FORMAT));
                    } else if (line.startsWith("DTEND:")) {
                        currentEvent.setEndDateTime(LocalDateTime.parse(line.substring(6), ICS_DATE_FORMAT));
                    }
                }
            }
        }
        return events;
    }

    private static JournalEntry parseJournalMarkdownFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        String title = null;
        LocalDate date = null;
        boolean inFrontMatter = false;
        boolean frontMatterProcessed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("---")) {
                    if (!frontMatterProcessed) {
                        inFrontMatter = !inFrontMatter;
                        if (!inFrontMatter) {
                            frontMatterProcessed = true;
                        }
                        continue;
                    }
                }

                if (inFrontMatter) {
                    if (line.startsWith("title:")) {
                        title = line.substring(6).trim();
                    } else if (line.startsWith("date:")) {
                        try {
                            date = LocalDate.parse(line.substring(5).trim());
                        } catch (Exception e) {
                            // Ignore date parse error
                        }
                    }
                    continue;
                }

                if (frontMatterProcessed || !line.trim().isEmpty()) {
                    content.append(line).append("\n");
                }
            }
        }

        if (title == null || title.isEmpty()) {
            title = file.getName().replaceAll("\\.md$", "").replaceAll("_", " ");
        }

        if (date == null) {
            // Try to parse date from filename (YYYY-MM-DD)
            try {
                String filename = file.getName().replaceAll("\\.md$", "");
                date = LocalDate.parse(filename);
            } catch (Exception e) {
                date = LocalDate.now();
            }
        }

        JournalEntry entry = new JournalEntry();
        entry.setTitle(title);
        entry.setContent(content.toString().trim());
        entry.setDate(date);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        return entry;
    }
}
