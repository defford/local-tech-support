package com.localtechsupport.dto.request;

import com.localtechsupport.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating ticket status.
 */
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @NotBlank(message = "Updated by is required")
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Default constructor
    public UpdateStatusRequest() {}

    // Constructor with all fields
    public UpdateStatusRequest(TicketStatus status, String reason, String updatedBy) {
        this.status = status;
        this.reason = reason;
        this.updatedBy = updatedBy;
    }

    // Getters and setters
    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
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
        return "UpdateStatusRequest{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
} 