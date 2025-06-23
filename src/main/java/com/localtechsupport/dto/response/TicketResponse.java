package com.localtechsupport.dto.response;

import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.TicketStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Complete response DTO for ticket information.
 * 
 * Contains full ticket details including related client and technician information.
 */
public class TicketResponse {

    private Long id;
    private ClientSummaryResponse client;
    private TechnicianSummaryResponse assignedTechnician;
    private ServiceType serviceType;
    private String description;
    private TicketStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant dueAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    // Default constructor
    public TicketResponse() {}

    // Constructor with all fields
    public TicketResponse(Long id, ClientSummaryResponse client, TechnicianSummaryResponse assignedTechnician,
                         ServiceType serviceType, String description, TicketStatus status,
                         Instant dueAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.client = client;
        this.assignedTechnician = assignedTechnician;
        this.serviceType = serviceType;
        this.description = description;
        this.status = status;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClientSummaryResponse getClient() {
        return client;
    }

    public void setClient(ClientSummaryResponse client) {
        this.client = client;
    }

    public TechnicianSummaryResponse getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(TechnicianSummaryResponse assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public boolean isOverdue() {
        return dueAt != null && Instant.now().isAfter(dueAt) && status == TicketStatus.OPEN;
    }

    public boolean isAssigned() {
        return assignedTechnician != null;
    }

    @Override
    public String toString() {
        return "TicketResponse{" +
                "id=" + id +
                ", client=" + client +
                ", assignedTechnician=" + assignedTechnician +
                ", serviceType=" + serviceType +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", dueAt=" + dueAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 