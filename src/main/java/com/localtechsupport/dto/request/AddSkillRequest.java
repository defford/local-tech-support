package com.localtechsupport.dto.request;

import com.localtechsupport.entity.ServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a skill to a technician.
 * 
 * Provides:
 * - Technician ID for skill assignment
 * - Service type for skill specification
 * - Input validation and sanitization
 * - JSON serialization support
 */
@Data
@NoArgsConstructor
public class AddSkillRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    private String certificationLevel;

    // Constructor with required field
    public AddSkillRequest(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    // Constructor with all fields
    public AddSkillRequest(ServiceType serviceType, String certificationLevel) {
        this.serviceType = serviceType;
        this.certificationLevel = certificationLevel;
    }

    /**
     * Constructor for convenient object creation.
     */
    public AddSkillRequest(Long technicianId, ServiceType serviceType) {
        this.technicianId = technicianId;
        this.serviceType = serviceType;
    }

    /**
     * Validates that all required fields are present and valid.
     */
    public boolean isValid() {
        return technicianId != null && 
               technicianId > 0 && 
               serviceType != null;
    }

    // Getters and Setters
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(String certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    /**
     * Returns a string representation for logging and debugging.
     */
    @Override
    public String toString() {
        return String.format("AddSkillRequest{technicianId=%d, serviceType=%s}", 
                           technicianId, serviceType);
    }
} 