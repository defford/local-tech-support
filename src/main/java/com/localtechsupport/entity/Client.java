package com.localtechsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be 50 characters or less")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be 50 characters or less")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be 100 characters or less")
    private String email;

    @Column(name = "phone")
    @Size(max = 20, message = "Phone number must be 20 characters or less")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private ClientStatus status = ClientStatus.ACTIVE;

    @Column(name = "address")
    @Size(max = 255, message = "Address must be 255 characters or less")
    private String address;

    @Column(name = "notes")
    @Size(max = 500, message = "Notes must be 500 characters or less")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ClientStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    public enum SupportLevel {
        BASIC,
        PREMIUM,
        ENTERPRISE
    }

    // lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Manual setter for email (Lombok seems to have an issue generating this)
    public void setEmail(String email) {
        this.email = email;
    }
    
    // Manual setters for timestamp fields (needed for tests, but normally managed by Hibernate)
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
