package com.roam.controller;

import com.roam.model.*;
import com.roam.repository.*;
import com.roam.service.WikiService;
import com.roam.service.WikiServiceImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class WikiController {

    private static final Logger logger = LoggerFactory.getLogger(WikiController.class);

    private final WikiService wikiService;
    private final WikiTemplateRepository templateRepository;
    private final OperationRepository operationRepository;
    private final RegionRepository regionRepository;
    private final TaskRepository taskRepository;
    private final CalendarEventRepository eventRepository;

    private Wiki currentNote;
    private final ObservableList<Wiki> allNotes;
    private final FilteredList<Wiki> filteredNotes;
    private final StringProperty searchQuery;
    private final ObjectProperty<Operation> selectedOperation;

    private Timeline autoSaveTimer;
    private final java.util.List<Consumer<Wiki>> noteChangedListeners = new java.util.ArrayList<>();

    public WikiController() {
        this.wikiService = new WikiServiceImpl();
        this.templateRepository = new WikiTemplateRepository();
        this.operationRepository = new OperationRepository();
        this.regionRepository = new RegionRepository();
        this.taskRepository = new TaskRepository();
        this.eventRepository = new CalendarEventRepository();

        this.allNotes = FXCollections.observableArrayList();
        this.filteredNotes = new FilteredList<>(allNotes, Wiki -> true);
        this.searchQuery = new SimpleStringProperty("");
        this.selectedOperation = new SimpleObjectProperty<>();

        setupAutoSave();
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timeline(new KeyFrame(Duration.seconds(2), event -> saveCurrentNote()));
        autoSaveTimer.setCycleCount(1);
    }

    public void scheduleAutoSave() {
        if (autoSaveTimer.getStatus() == Timeline.Status.RUNNING) {
            autoSaveTimer.stop();
        }
        autoSaveTimer.playFromStart();
    }

    public List<Wiki> loadAllNotes() {
        List<Wiki> notes = wikiService.findAll();
        allNotes.setAll(notes);
        return notes;
    }

    public List<Wiki> loadRecentNotes(int limit) {
        return wikiService.findRecent(limit);
    }

    public List<Wiki> loadFavoriteNotes() {
        return wikiService.findFavorites();
    }

    public List<Wiki> loadNotesForOperation(Operation op) {
        if (op == null)
            return List.of();
        return wikiService.findByOperationId(op.getId());
    }

    public List<Wiki> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return loadAllNotes();
        }
        searchQuery.set(query);
        return wikiService.findAll(); // Service handles search via Lucene
    }

    public Wiki createNewNote() {
        Wiki Wiki = new Wiki();
        Wiki.setTitle("Untitled");
        Wiki.setContent("");
        Wiki.setCreatedAt(LocalDateTime.now());
        Wiki.setUpdatedAt(LocalDateTime.now());
        Wiki.setIsFavorite(false);
        Wiki.setWordCount(0);

        Wiki saved = wikiService.createWiki(Wiki);
        allNotes.add(0, saved);
        setCurrentNote(saved);

        return saved;
    }

    public Wiki createNoteFromTemplate(WikiTemplate template) {
        Wiki Wiki = new Wiki();
        Wiki.setTitle(template.getName());
        Wiki.setContent(template.processTemplate(template.getName(), null));
        Wiki.setTemplateId(template.getId());
        Wiki.setCreatedAt(LocalDateTime.now());
        Wiki.setUpdatedAt(LocalDateTime.now());
        Wiki.setIsFavorite(false);
        Wiki.calculateWordCount();

        Wiki saved = wikiService.createWiki(Wiki);
        allNotes.add(0, saved);
        setCurrentNote(saved);

        return saved;
    }

    public void saveCurrentNote() {
        if (currentNote == null)
            return;

        currentNote.setUpdatedAt(LocalDateTime.now());
        currentNote.calculateWordCount();
        wikiService.updateWiki(currentNote);

        // Refresh in list
        int index = allNotes.indexOf(currentNote);
        if (index >= 0) {
            allNotes.set(index, currentNote);
        }
    }

    public void deleteNote(Wiki Wiki) {
        if (Wiki == null)
            return;

        wikiService.deleteWiki(Wiki.getId());
        allNotes.remove(Wiki);

        if (currentNote != null && currentNote.getId().equals(Wiki.getId())) {
            setCurrentNote(null);
        }
    }

    public void toggleFavorite(Wiki Wiki) {
        if (Wiki == null)
            return;

        Wiki updated = wikiService.toggleFavorite(Wiki.getId());

        // Refresh in list
        int index = allNotes.indexOf(Wiki);
        if (index >= 0) {
            allNotes.set(index, updated);
        }

        // If this is the current note, update reference and trigger change notification
        if (currentNote != null && currentNote.getId().equals(Wiki.getId())) {
            currentNote = updated;
            notifyNoteChanged(updated);
        }
    }

    public Wiki duplicateNote(Wiki original) {
        if (original == null)
            return null;

        Wiki duplicate = new Wiki();
        duplicate.setTitle(original.getTitle() + " (Copy)");
        duplicate.setContent(original.getContent());
        duplicate.setRegion(original.getRegion());
        duplicate.setOperationId(original.getOperationId());
        duplicate.setTaskId(original.getTaskId());
        duplicate.setCalendarEventId(original.getCalendarEventId());
        duplicate.setBannerUrl(original.getBannerUrl());
        duplicate.setTemplateId(original.getTemplateId());
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());
        duplicate.setIsFavorite(false);
        duplicate.calculateWordCount();

        Wiki saved = wikiService.createWiki(duplicate);
        allNotes.add(0, saved);
        setCurrentNote(saved);

        return saved;
    }

    public List<Operation> loadAllOperations() {
        return operationRepository.findAll();
    }

    public void openNoteByTitle(String title) {
        // Find Wiki by title
        for (Wiki Wiki : allNotes) {
            if (Wiki.getTitle().equalsIgnoreCase(title)) {
                setCurrentNote(Wiki);
                return;
            }
        }

        // Wiki not found - could implement a feature to ask user to create it
    }

    public List<WikiTemplate> loadAllTemplates() {
        return templateRepository.findAll();
    }

    public List<WikiTemplate> loadDefaultTemplates() {
        return templateRepository.findDefaults();
    }

    public List<WikiTemplate> loadCustomTemplates() {
        return templateRepository.findCustom();
    }

    public WikiTemplate createTemplate(String name, String description, String content, String icon) {
        WikiTemplate template = new WikiTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setContent(content);
        template.setIcon(icon);
        template.setIsDefault(false);
        template.setCreatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public WikiTemplate updateTemplate(WikiTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public void deleteTemplate(WikiTemplate template) {
        templateRepository.delete(template.getId());
    }

    public long getTotalWordCount() {
        // Calculate from all loaded notes
        return allNotes.stream()
                .mapToLong(Wiki::getWordCount)
                .sum();
    }

    // Getters and setters
    public Wiki getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(Wiki Wiki) {
        this.currentNote = Wiki;
        notifyNoteChanged(Wiki);
    }

    public ObservableList<Wiki> getAllNotes() {
        return allNotes;
    }

    public FilteredList<Wiki> getFilteredNotes() {
        return filteredNotes;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public ObjectProperty<Operation> selectedOperationProperty() {
        return selectedOperation;
    }

    public void addOnNoteChangedListener(Consumer<Wiki> handler) {
        this.noteChangedListeners.add(handler);
    }

    private void notifyNoteChanged(Wiki wiki) {
        for (Consumer<Wiki> listener : noteChangedListeners) {
            listener.accept(wiki);
        }
    }

    public List<Region> loadAllRegions() {
        return regionRepository.findAll();
    }

    public List<Task> loadAllTasks() {
        return taskRepository.findAll();
    }

    public List<CalendarEvent> loadAllEvents() {
        return eventRepository.findAll();
    }
}
