package com.roam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.CalendarEvent;
import com.roam.model.JournalEntry;
import com.roam.model.Operation;
import com.roam.model.Wiki;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static void exportNoteToMarkdown(Wiki Wiki, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Wiki");
        fileChooser.setInitialFileName(Wiki.getTitle() + ".md");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File file = fileChooser.showSaveDialog(owner);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Add metadata header (YAML front matter style)
                writer.write("---\n");
                writer.write("title: " + Wiki.getTitle() + "\n");
                writer.write("date: " + Wiki.getCreatedAt() + "\n");
                writer.write("updated: " + Wiki.getUpdatedAt() + "\n");
                // Tags disabled - replaced with regions
                // if (!Wiki.getTags().isEmpty()) {
                // writer.write("tags: [");
                // writer.write(String.join(", ", Wiki.getTags().stream()
                // .map(tag -> tag.getName()).toArray(String[]::new)));
                // writer.write("]\n");
                // }
                if (Wiki.getRegion() != null && !Wiki.getRegion().isEmpty()) {
                    writer.write("region: " + Wiki.getRegion() + "\n");
                }
                writer.write("---\n\n");

                writer.write(Wiki.getContent() != null ? Wiki.getContent() : "");
                DialogUtils.showSuccess("Wiki exported successfully!");
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    public static void exportAllNotesToMarkdown(Window owner, java.util.List<Wiki> notes) {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Folder");

        File dir = dirChooser.showDialog(owner);
        if (dir != null) {
            int count = 0;
            for (Wiki Wiki : notes) {
                try {
                    String filename = Wiki.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".md";
                    File file = new File(dir, filename);
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("---\n");
                        writer.write("title: " + Wiki.getTitle() + "\n");
                        writer.write("date: " + Wiki.getCreatedAt() + "\n");
                        writer.write("---\n\n");
                        writer.write(Wiki.getContent() != null ? Wiki.getContent() : "");
                    }
                    count++;
                } catch (IOException e) {
                    logger.error("Failed to export: {}", Wiki.getTitle(), e);
                }
            }
            DialogUtils.showSuccess(count + " wikis exported to " + dir.getPath());
        }
    }

    public static void exportEventsToICS(Window owner, List<CalendarEvent> events) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Events");
        fileChooser.setInitialFileName("events.ics");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ICS Files", "*.ics"));

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("BEGIN:VCALENDAR\n");
                writer.write("VERSION:2.0\n");
                writer.write("PRODID:-//Roam//Roam Calendar//EN\n");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

                for (CalendarEvent event : events) {
                    writer.write("BEGIN:VEVENT\n");
                    writer.write("UID:" + event.getId() + "@roam.app\n");
                    writer.write("DTSTAMP:" + java.time.LocalDateTime.now().format(formatter) + "\n");
                    writer.write("DTSTART:" + event.getStartDateTime().format(formatter) + "\n");
                    writer.write("DTEND:" + event.getEndDateTime().format(formatter) + "\n");
                    writer.write("SUMMARY:" + escapeICS(event.getTitle()) + "\n");
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        writer.write("DESCRIPTION:" + escapeICS(event.getDescription()) + "\n");
                    }
                    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                        writer.write("LOCATION:" + escapeICS(event.getLocation()) + "\n");
                    }
                    writer.write("END:VEVENT\n");
                }

                writer.write("END:VCALENDAR\n");
                DialogUtils.showSuccess("Events exported successfully!");
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    private static String escapeICS(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    public static void exportOperationsToJSON(Window owner, List<Operation> operations) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Operations");
        fileChooser.setInitialFileName("operations.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                objectMapper.writeValue(file, operations);
                DialogUtils.showSuccess("Operations exported successfully!");
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    public static void exportJournalsToMarkdown(Window owner, List<JournalEntry> entries) {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Folder for Journals");

        File dir = dirChooser.showDialog(owner);
        if (dir != null) {
            int count = 0;
            for (JournalEntry entry : entries) {
                try {
                    String filename = "Journal_" + entry.getDate().toString() + ".md";
                    File file = new File(dir, filename);
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("---\n");
                        writer.write("title: " + entry.getTitle() + "\n");
                        writer.write("date: " + entry.getDate() + "\n");
                        writer.write("---\n\n");
                        writer.write(entry.getContent() != null ? entry.getContent() : "");
                    }
                    count++;
                } catch (IOException e) {
                    logger.error("Failed to export journal: {}", entry.getTitle(), e);
                }
            }
            DialogUtils.showSuccess(count + " journals exported to " + dir.getPath());
        }
    }
}
