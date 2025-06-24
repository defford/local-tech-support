package com.localtechsupport.dto.request;

import com.localtechsupport.entity.Client;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating client status.
 * Used for status transitions like ACTIVE to INACTIVE or vice versa.
 */
public class UpdateClientStatusRequest {

    @NotNull(message = "Status is required")
    private Client.ClientStatus status;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Default constructor
    public UpdateClientStatusRequest() {}

    // Constructor with required fields
    public UpdateClientStatusRequest(Client.ClientStatus status) {
        this.status = status;
    }

    // Constructor with all fields
    public UpdateClientStatusRequest(Client.ClientStatus status, String reason, String updatedBy) {
        this.status = status;
        this.reason = reason;
        this.updatedBy = updatedBy;
    }

    // Utility methods
    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public boolean hasUpdatedBy() {
        return updatedBy != null && !updatedBy.trim().isEmpty();
    }

    // Getters and Setters
    public Client.ClientStatus getStatus() {
        return status;
    }

    public void setStatus(Client.ClientStatus status) {
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
        return "UpdateClientStatusRequest{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
} 