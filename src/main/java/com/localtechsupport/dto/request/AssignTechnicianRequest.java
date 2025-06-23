package com.localtechsupport.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for assigning a technician to a ticket.
 */
public class AssignTechnicianRequest {

    @NotNull(message = "Technician ID is required")
    @Positive(message = "Technician ID must be positive")
    private Long technicianId;

    // Default constructor
    public AssignTechnicianRequest() {}

    // Constructor
    public AssignTechnicianRequest(Long technicianId) {
        this.technicianId = technicianId;
    }

    // Getters and setters
    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    @Override
    public String toString() {
        return "AssignTechnicianRequest{" +
                "technicianId=" + technicianId +
                '}';
    }
} 