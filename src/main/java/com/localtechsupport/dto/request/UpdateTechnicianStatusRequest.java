package com.localtechsupport.dto.request;

import com.localtechsupport.entity.TechnicianStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating technician status.
 * Contains status transition information and audit details.
 */
public class UpdateTechnicianStatusRequest {

    @NotNull(message = "Status is required")
    private TechnicianStatus status;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @NotBlank(message = "Updated by is required")
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Default constructor
    public UpdateTechnicianStatusRequest() {}

    // Constructor with all fields
    public UpdateTechnicianStatusRequest(TechnicianStatus status, String reason, String updatedBy) {
        this.status = status;
        this.reason = reason;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public TechnicianStatus getStatus() {
        return status;
    }

    public void setStatus(TechnicianStatus status) {
        this.status = status;
    }

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
        return "UpdateTechnicianStatusRequest{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
} 