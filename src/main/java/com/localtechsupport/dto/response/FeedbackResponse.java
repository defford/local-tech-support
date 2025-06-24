package com.localtechsupport.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.localtechsupport.entity.FeedbackEntry;

import java.time.Instant;

/**
 * Response DTO for FeedbackEntry entity.
 * Contains all feedback details for API responses.
 */
public class FeedbackResponse {

    private Long id;
    private Long ticketId;
    private String ticketDescription;
    private int rating;
    private String comment;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant submittedAt;
    
    private String createdBy;

    // Default constructor
    public FeedbackResponse() {}

    // Constructor from entity
    public FeedbackResponse(FeedbackEntry feedback) {
        this.id = feedback.getId();
        this.ticketId = feedback.getTicket().getId();
        this.ticketDescription = feedback.getTicket().getDescription();
        this.rating = feedback.getRating();
        this.comment = feedback.getComment();
        this.submittedAt = feedback.getSubmittedAt();
        this.createdBy = feedback.getCreatedBy();
    }

    // Static factory method
    public static FeedbackResponse from(FeedbackEntry feedback) {
        return new FeedbackResponse(feedback);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public void setTicketDescription(String ticketDescription) {
        this.ticketDescription = ticketDescription;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "FeedbackResponse{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", rating=" + rating +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50)) + "..." : null) + '\'' +
                ", submittedAt=" + submittedAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
} 