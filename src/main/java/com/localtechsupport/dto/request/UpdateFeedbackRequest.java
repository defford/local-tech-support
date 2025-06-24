package com.localtechsupport.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing feedback entry.
 * Contains optional fields that can be updated.
 */
public class UpdateFeedbackRequest {

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    // Default constructor
    public UpdateFeedbackRequest() {}

    // Constructor with optional fields
    public UpdateFeedbackRequest(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "UpdateFeedbackRequest{" +
                "rating=" + rating +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50)) + "..." : null) + '\'' +
                '}';
    }
} 