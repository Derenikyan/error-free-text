package com.dirinekyan.eft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "correction_tasks")
public class CorrectionTask {

    private static final int MAX_ERROR_LENGTH = 1000;

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "corrected_text", columnDefinition = "TEXT")
    private String correctedText;

    @Column(name = "error_message", length = MAX_ERROR_LENGTH)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CorrectionTask() {
    }

    public CorrectionTask(String text, Language language) {
        this.id = UUID.randomUUID();
        this.text = text;
        this.language = language;
        this.status = TaskStatus.NEW;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void complete(String correctedText) {
        this.status = TaskStatus.COMPLETED;
        this.correctedText = correctedText;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.status = TaskStatus.ERROR;
        this.errorMessage = errorMessage == null || errorMessage.length() <= MAX_ERROR_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_LENGTH);
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Language getLanguage() {
        return language;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
