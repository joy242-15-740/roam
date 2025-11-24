package com.roam.model;

import java.util.ArrayList;
import java.util.List;

public class TaskFilter {

    private List<Long> operationIds;
    private List<TaskStatus> statuses;
    private List<Priority> priorities;
    private List<String> assignees;
    private DueDateFilter dueDateFilter;
    private String searchQuery;
    private TaskSortField sortBy;
    private SortOrder sortOrder;
    private Boolean showCompleted;

    public enum DueDateFilter {
        ANY,
        OVERDUE,
        TODAY,
        TOMORROW,
        THIS_WEEK,
        THIS_MONTH,
        NO_DUE_DATE,
        HAS_DUE_DATE
    }

    public enum TaskSortField {
        CREATED_AT,
        UPDATED_AT,
        DUE_DATE,
        PRIORITY,
        TITLE,
        STATUS,
        OPERATION
    }

    public enum SortOrder {
        ASC,
        DESC
    }

    public TaskFilter() {
        this.operationIds = new ArrayList<>();
        this.statuses = new ArrayList<>();
        this.priorities = new ArrayList<>();
        this.assignees = new ArrayList<>();
        this.dueDateFilter = DueDateFilter.ANY;
        this.searchQuery = null;
        this.sortBy = TaskSortField.CREATED_AT;
        this.sortOrder = SortOrder.DESC;
        this.showCompleted = true;
    }

    public boolean isFilterActive() {
        return !operationIds.isEmpty() ||
                !statuses.isEmpty() ||
                !priorities.isEmpty() ||
                !assignees.isEmpty() ||
                dueDateFilter != DueDateFilter.ANY ||
                (searchQuery != null && !searchQuery.trim().isEmpty()) ||
                !showCompleted;
    }

    public void reset() {
        operationIds.clear();
        statuses.clear();
        priorities.clear();
        assignees.clear();
        dueDateFilter = DueDateFilter.ANY;
        searchQuery = null;
        sortBy = TaskSortField.CREATED_AT;
        sortOrder = SortOrder.DESC;
        showCompleted = true;
    }

    public TaskFilter clone() {
        TaskFilter copy = new TaskFilter();
        copy.operationIds = new ArrayList<>(this.operationIds);
        copy.statuses = new ArrayList<>(this.statuses);
        copy.priorities = new ArrayList<>(this.priorities);
        copy.assignees = new ArrayList<>(this.assignees);
        copy.dueDateFilter = this.dueDateFilter;
        copy.searchQuery = this.searchQuery;
        copy.sortBy = this.sortBy;
        copy.sortOrder = this.sortOrder;
        copy.showCompleted = this.showCompleted;
        return copy;
    }

    // Getters and Setters
    public List<Long> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(List<Long> operationIds) {
        this.operationIds = operationIds;
    }

    public List<TaskStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<TaskStatus> statuses) {
        this.statuses = statuses;
    }

    public List<Priority> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }

    public DueDateFilter getDueDateFilter() {
        return dueDateFilter;
    }

    public void setDueDateFilter(DueDateFilter dueDateFilter) {
        this.dueDateFilter = dueDateFilter;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public TaskSortField getSortBy() {
        return sortBy;
    }

    public void setSortBy(TaskSortField sortBy) {
        this.sortBy = sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getShowCompleted() {
        return showCompleted;
    }

    public void setShowCompleted(Boolean showCompleted) {
        this.showCompleted = showCompleted;
    }
}
