package com.localtechsupport.dto.request;

import com.localtechsupport.entity.AppointmentStatus;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for updating existing appointments.
 * 
 * Allows updating:
 * - Appointment status
 * - Start/end times (with validation)
 * - Notes or reason for changes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private AppointmentStatus status;

    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @Future(message = "End time must be in the future") 
    private Instant endTime;

    private String reason;

    private String updatedBy;
} 