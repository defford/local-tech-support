package com.localtechsupport.dto.response;

/**
 * Summary response DTO for technician information in ticket responses.
 */
public class TechnicianSummaryResponse {

    private Long id;
    private String fullName;
    private String email;

    // Default constructor
    public TechnicianSummaryResponse() {}

    // Constructor with all fields
    public TechnicianSummaryResponse(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters and setters
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

    @Override
    public String toString() {
        return "TechnicianSummaryResponse{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
} 