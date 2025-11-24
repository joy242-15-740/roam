package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "Wiki_templates")
public class WikiTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 10)
    private String icon;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Template placeholders
    public static final String PLACEHOLDER_DATE = "{date}";
    public static final String PLACEHOLDER_TIME = "{time}";
    public static final String PLACEHOLDER_DATETIME = "{datetime}";
    public static final String PLACEHOLDER_TITLE = "{title}";
    public static final String PLACEHOLDER_OPERATION = "{operation}";

    public WikiTemplate() {
    }

    public WikiTemplate(String name, String description, String content, String icon, boolean isDefault) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String processTemplate(String title, String operationName) {
        String processed = content;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        processed = processed.replace(PLACEHOLDER_DATE, now.format(dateFormatter));
        processed = processed.replace(PLACEHOLDER_TIME, now.format(timeFormatter));
        processed = processed.replace(PLACEHOLDER_DATETIME, now.format(dateTimeFormatter));
        processed = processed.replace(PLACEHOLDER_TITLE, title != null ? title : "");
        processed = processed.replace(PLACEHOLDER_OPERATION, operationName != null ? operationName : "");

        return processed;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "WikiTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
