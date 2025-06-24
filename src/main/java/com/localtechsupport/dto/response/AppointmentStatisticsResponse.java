package com.localtechsupport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for appointment statistics and analytics.
 * 
 * Provides comprehensive metrics for:
 * - Appointment counts by status
 * - Technician utilization
 * - Scheduling efficiency metrics
 * - Time-based analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatisticsResponse {

    // Overall counts
    private long totalAppointments;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long noShowAppointments;

    // Time-based metrics
    private long upcomingAppointments;
    private long missedAppointments;
    private long todayAppointments;
    private long thisWeekAppointments;

    // Utilization metrics
    private Map<Long, Long> appointmentsByTechnician;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> appointmentsByServiceType;

    // Efficiency metrics
    private double averageAppointmentDuration; // in hours
    private double completionRate; // percentage of completed vs total
    private double noShowRate; // percentage of no-shows vs total
    private double utilizationRate; // percentage of available time slots used

    // Recent trends
    private long appointmentsLastWeek;
    private long appointmentsThisWeek;
    private double weekOverWeekChange; // percentage change
} 