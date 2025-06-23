package com.localtechsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for closing a ticket with resolution details.
 */
public class CloseTicketRequest {

    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    private String resolutionNotes;

    @NotBlank(message = "Closed by is required")
    @Size(max = 100, message = "Closed by must not exceed 100 characters")
    private String closedBy;

    // Default constructor
    public CloseTicketRequest() {}

    // Constructor with all fields
    public CloseTicketRequest(String resolutionNotes, String closedBy) {
        this.resolutionNotes = resolutionNotes;
        this.closedBy = closedBy;
    }

    // Getters and setters
    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    @Override
    public String toString() {
        return "CloseTicketRequest{" +
                "resolutionNotes='" + resolutionNotes + '\'' +
                ", closedBy='" + closedBy + '\'' +
                '}';
    }
} 