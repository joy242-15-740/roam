package com.roam.layout;

import com.roam.controller.*;
import com.roam.view.*;
import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * Factory for creating views with their controllers.
 * Centralizes view instantiation and dependency injection.
 */
public class ViewFactory {

    public ViewFactory() {
    }

    /**
     * Creates the Operations view with its controller.
     *
     * @param onOperationClick Callback when an operation is clicked
     * @return The created OperationsView
     */
    public Node createOperationsView(Consumer<com.roam.model.Operation> onOperationClick) {
        OperationsController controller = new OperationsController();
        OperationsView view = new OperationsView(controller);
        view.setOnOperationClick(onOperationClick);
        return view;
    }

    /**
     * Creates the Operation Detail view for a specific operation.
     *
     * @param operation   The operation to display
     * @param onBackClick Callback when back button is clicked
     * @return The created OperationDetailView
     */
    public Node createOperationDetailView(com.roam.model.Operation operation, Runnable onBackClick) {
        return new com.roam.view.OperationDetailView(operation, onBackClick);
    }

    /**
     * Creates the Calendar view with its controller.
     *
     * @return The created CalendarView
     */
    public Node createCalendarView() {
        CalendarController controller = new CalendarController();
        return new CalendarView(controller);
    }

    /**
     * Creates the Tasks view with its controller.
     *
     * @return The created TasksView
     */
    public Node createTasksView() {
        TasksController controller = new TasksController();
        return new TasksView(controller);
    }

    /**
     * Creates the Wiki view with its controller.
     *
     * @return The created WikiView
     */
    public Node createWikiView() {
        WikiController controller = new WikiController();
        return new WikiView(controller);
    }

    /**
     * Creates the Journal view.
     *
     * @return The created JournalView
     */
    public Node createJournalView() {
        return new JournalView();
    }

    /**
     * Creates the Statistics view with its controller.
     *
     * @return The created StatisticsView
     */
    public Node createStatisticsView() {
        WikiController controller = new WikiController();
        return new StatisticsView(controller);
    }

    /**
     * Creates the Settings view.
     *
     * @return The created SettingsView
     */
    public Node createSettingsView() {
        return new SettingsView();
    }

    /**
     * Creates the Search view with its controller.
     * Note: This returns an empty SearchView that should be populated with results
     * by the caller.
     *
     * @param onResultClick Callback when a search result is clicked
     * @return The created SearchView
     */
    public SearchView createSearchView(Consumer<com.roam.service.SearchService.SearchResult> onResultClick) {
        // Create empty search view - will be populated by NavigationManager
        SearchView searchView = new SearchView(java.util.Collections.emptyList(), "");
        searchView.setOnResultSelected(onResultClick);
        return searchView;
    }
}
