package com.localtechsupport.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.TechnicianStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for Technician information.
 * Contains complete technician details including skills and workload.
 */
public class TechnicianResponse {

    private Long id;
    private String fullName;
    private String email;
    private TechnicianStatus status;
    private Set<ServiceType> skills;
    private long currentWorkload;
    private boolean available;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Default constructor
    public TechnicianResponse() {}

    // Constructor with essential fields
    public TechnicianResponse(Long id, String fullName, String email, TechnicianStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    // Constructor with all fields
    public TechnicianResponse(Long id, String fullName, String email, TechnicianStatus status, 
                            Set<ServiceType> skills, long currentWorkload, boolean available,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.skills = skills;
        this.currentWorkload = currentWorkload;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TechnicianStatus getStatus() {
        return status;
    }

    public void setStatus(TechnicianStatus status) {
        this.status = status;
    }

    public Set<ServiceType> getSkills() {
        return skills;
    }

    public void setSkills(Set<ServiceType> skills) {
        this.skills = skills;
    }

    public long getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(long currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TechnicianResponse{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", skills=" + skills +
                ", currentWorkload=" + currentWorkload +
                ", available=" + available +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 