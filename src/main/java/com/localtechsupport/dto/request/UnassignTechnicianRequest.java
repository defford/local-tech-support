package com.localtechsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for unassigning a technician from a ticket.
 */
public class UnassignTechnicianRequest {

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @NotBlank(message = "Updated by is required")
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Default constructor
    public UnassignTechnicianRequest() {}

    // Constructor with all fields
    public UnassignTechnicianRequest(String reason, String updatedBy) {
        this.reason = reason;
        this.updatedBy = updatedBy;
    }

    // Getters and setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "UnassignTechnicianRequest{" +
                "reason='" + reason + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
} 