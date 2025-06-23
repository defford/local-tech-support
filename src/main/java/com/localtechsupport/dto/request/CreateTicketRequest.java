package com.localtechsupport.dto.request;

import com.localtechsupport.entity.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new support ticket.
 * 
 * Contains all required information for ticket creation with validation.
 */
public class CreateTicketRequest {

    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be positive")
    private Long clientId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    // Default constructor
    public CreateTicketRequest() {}

    // Constructor with all fields
    public CreateTicketRequest(Long clientId, ServiceType serviceType, String description) {
        this.clientId = clientId;
        this.serviceType = serviceType;
        this.description = description;
    }

    // Getters and setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreateTicketRequest{" +
                "clientId=" + clientId +
                ", serviceType=" + serviceType +
                ", description='" + description + '\'' +
                '}';
    }
} 