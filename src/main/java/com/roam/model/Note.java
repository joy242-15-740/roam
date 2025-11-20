package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id")
    private Long operationId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_favorite", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFavorite = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "word_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer wordCount = 0;

    @Column(name = "linked_note_ids", columnDefinition = "TEXT")
    private String linkedNoteIds;

    // Constructors
    public Note() {
        this.title = "Untitled Note";
        this.content = "";
        this.isFavorite = false;
        this.wordCount = 0;
        this.tags = new HashSet<>();
    }

    public Note(String title, Long operationId) {
        this.title = title;
        this.operationId = operationId;
        this.content = "";
        this.isFavorite = false;
        this.wordCount = 0;
        this.tags = new HashSet<>();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateWordCount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateWordCount();
    }

    public Integer calculateWordCount() {
        if (content == null || content.trim().isEmpty()) {
            this.wordCount = 0;
            return 0;
        }

        String[] words = content.trim().split("\\s+");
        this.wordCount = words.length;
        return this.wordCount;
    }

    public boolean addTag(Tag tag) {
        return tags.add(tag);
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getLinkedNoteIds() {
        return linkedNoteIds;
    }

    public void setLinkedNoteIds(String linkedNoteIds) {
        this.linkedNoteIds = linkedNoteIds;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", operationId=" + operationId +
                '}';
    }
}
