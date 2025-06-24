package com.localtechsupport.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new feedback entry.
 * Contains all required fields for feedback submission.
 */
public class CreateFeedbackRequest {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    @NotBlank(message = "Created by is required")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    // Default constructor
    public CreateFeedbackRequest() {}

    // Constructor with required fields
    public CreateFeedbackRequest(Long ticketId, Integer rating, String comment, String createdBy) {
        this.ticketId = ticketId;
        this.rating = rating;
        this.comment = comment;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "CreateFeedbackRequest{" +
                "ticketId=" + ticketId +
                ", rating=" + rating +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50)) + "..." : null) + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
} 