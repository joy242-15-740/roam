package com.roam.controller;

import com.roam.model.*;
import com.roam.service.CalendarService;
import com.roam.service.CalendarServiceImpl;
import com.roam.repository.*;
import com.roam.util.DialogUtils;
import com.roam.view.components.EventDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    private final CalendarService calendarService;
    private final CalendarEventRepository eventRepository; // Keep for direct queries
    private final CalendarSourceRepository sourceRepository;
    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final RegionRepository regionRepository;
    private final WikiRepository WikiRepository;

    private List<CalendarSource> calendarSources;
    private List<CalendarEvent> allEvents;

    private Runnable onDataChanged;

    public CalendarController() {
        this.calendarService = new CalendarServiceImpl();
        this.eventRepository = new CalendarEventRepository();
        this.sourceRepository = new CalendarSourceRepository();
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.regionRepository = new RegionRepository();
        this.WikiRepository = new WikiRepository();

        initialize();
    }

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    private void initialize() {
        logger.info("🗓️ Initializing Calendar...");

        // Create default calendar sources if not exist
        createDefaultCalendarSources();

        // Load all calendar sources
        calendarSources = sourceRepository.findAll();
        logger.info("✓ Loaded {} calendar sources", calendarSources.size());

        // Load all events
        loadAllEvents();

        // Sync tasks to events
        syncTasksToEvents();

        logger.info("✓ Calendar initialized with {} events", allEvents.size());
    }

    private void createDefaultCalendarSources() {
        List<CalendarSource> existing = sourceRepository.findAll();
        List<String> existingNames = existing.stream()
                .map(CalendarSource::getName)
                .collect(Collectors.toList());

        createSourceIfNotExists(existingNames, "Personal", "#4285f4", CalendarSourceType.PERSONAL, true);
        createSourceIfNotExists(existingNames, "Work", "#F4B400", CalendarSourceType.WORK, false);
        createSourceIfNotExists(existingNames, "Operations", "#0F9D58", CalendarSourceType.OPERATIONS, false);

        // New Regions
        createSourceIfNotExists(existingNames, "Lifestyle", "#FF5722", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Knowledge", "#9C27B0", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Skill", "#795548", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Spirituality", "#607D8B", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Relationship", "#E91E63", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Social", "#FF9800", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Career", "#3F51B5", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Finance", "#8BC34A", CalendarSourceType.CUSTOM, false);
        createSourceIfNotExists(existingNames, "Academic", "#00BCD4", CalendarSourceType.CUSTOM, false);
    }

    private void createSourceIfNotExists(List<String> existingNames, String name, String color, CalendarSourceType type,
            boolean isDefault) {
        if (!existingNames.contains(name)) {
            logger.debug("Creating calendar source: {}", name);
            CalendarSource source = new CalendarSource(name, color, type);
            source.setIsDefault(isDefault);
            sourceRepository.save(source);
        }
    }

    private void loadAllEvents() {
        allEvents = calendarService.findAll();
    }

    private void syncTasksToEvents() {
        try {
            // Get operations calendar
            CalendarSource operationsCal = calendarSources.stream()
                    .filter(s -> s.getType() == CalendarSourceType.OPERATIONS)
                    .findFirst()
                    .orElse(null);

            if (operationsCal == null) {
                return;
            }

            // Get all tasks with due dates
            // Wiki: You'll need to add a findAll() method to TaskRepository
            // For now, we'll skip this and implement it later

        } catch (Exception e) {
            logger.error("Failed to sync tasks: {}", e.getMessage(), e);
        }
    }

    public List<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    public CalendarSource getCalendarSourceById(Long id) {
        return calendarSources.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<CalendarEvent> getEventsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return allEvents.stream()
                .filter(e -> {
                    CalendarSource source = getCalendarSourceById(e.getCalendarSourceId());
                    return source != null && source.getIsVisible();
                })
                .filter(e -> {
                    LocalDateTime eventStart = e.getStartDateTime();
                    LocalDateTime eventEnd = e.getEndDateTime();

                    // Event overlaps with this day
                    return !eventStart.isAfter(endOfDay) && !eventEnd.isBefore(startOfDay);
                })
                .collect(Collectors.toList());
    }

    public List<CalendarEvent> getAllEvents() {
        return allEvents.stream()
                .filter(e -> {
                    CalendarSource source = getCalendarSourceById(e.getCalendarSourceId());
                    return source != null && source.getIsVisible();
                })
                .collect(Collectors.toList());
    }

    public void createEvent(LocalDate date) {
        try {
            CalendarEvent event = new CalendarEvent();

            if (date != null) {
                event.setStartDateTime(date.atTime(9, 0));
                event.setEndDateTime(date.atTime(10, 0));
            } else {
                event.setStartDateTime(LocalDateTime.now().plusHours(1));
                event.setEndDateTime(LocalDateTime.now().plusHours(2));
            }

            List<Operation> operations = operationRepository.findAll();

            EventDialog dialog = new EventDialog(
                    event,
                    calendarSources,
                    operations,
                    regionRepository.findAll(),
                    taskRepository.findAll(),
                    WikiRepository.findAll(),
                    null);
            dialog.showAndWait().ifPresent(newEvent -> {
                try {
                    calendarService.createEvent(newEvent);
                    loadAllEvents();
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                } catch (Exception e) {
                    DialogUtils.showError("Save Error", "Failed to create event", e.getMessage());
                }
            });

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open event dialog", e.getMessage());
        }
    }

    public void editEvent(CalendarEvent event) {
        try {
            List<Operation> operations = operationRepository.findAll();

            EventDialog dialog = new EventDialog(
                    event,
                    calendarSources,
                    operations,
                    regionRepository.findAll(),
                    taskRepository.findAll(),
                    WikiRepository.findAll(),
                    () -> deleteEvent(event));

            dialog.showAndWait().ifPresent(updatedEvent -> {
                logger.debug("Saving edited event: {}", updatedEvent.getTitle());
                try {
                    calendarService.updateEvent(updatedEvent);
                    loadAllEvents();
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                } catch (Exception e) {
                    DialogUtils.showError("Save Error", "Failed to update event", e.getMessage());
                }
            });

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open event dialog", e.getMessage());
        }
    }

    public void deleteEvent(CalendarEvent event) {
        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Event",
                "Are you sure you want to delete this event?",
                "Event: " + event.getTitle());

        if (confirmed) {
            try {
                calendarService.deleteEvent(event.getId());
                loadAllEvents();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                DialogUtils.showError("Delete Error", "Failed to delete event", e.getMessage());
            }
        }
    }

    public void toggleCalendarVisibility(Long sourceId, boolean visible) {
        try {
            Optional<CalendarSource> sourceOpt = sourceRepository.findById(sourceId);
            sourceOpt.ifPresent(source -> {
                source.setIsVisible(visible);
                sourceRepository.save(source);

                // Update local list
                calendarSources = sourceRepository.findAll();
            });
        } catch (Exception e) {
            logger.error("Failed to toggle calendar visibility: {}", e.getMessage(), e);
        }
    }
}
