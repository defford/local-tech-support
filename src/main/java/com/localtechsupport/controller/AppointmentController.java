package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Appointment management operations.
 * 
 * Provides comprehensive API endpoints for:
 * - Appointment scheduling and conflict detection
 * - Status management and workflow transitions
 * - Calendar views and technician schedules
 * - Availability checking and time slot management
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Create a new appointment.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        Appointment appointment = appointmentService.createAppointment(
            request.getTechnicianId(),
            request.getTicketId(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get an appointment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.findById(id);
        
        if (appointment.isPresent()) {
            AppointmentResponse response = mapToAppointmentResponse(appointment.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all appointments with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) Long technicianId) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointments;
        
        // Apply filters based on query parameters
        if (status != null) {
            appointments = appointmentService.findAppointmentsByStatus(status, pageable);
        } else if (technicianId != null) {
            appointments = appointmentService.findAppointmentsByTechnician(technicianId, pageable);
        } else {
            appointments = appointmentService.findAllAppointments(pageable);
        }
        
        Page<AppointmentResponse> response = appointments.map(this::mapToAppointmentResponse);
        return ResponseEntity.ok(response);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Update appointment status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        
        Appointment appointment = appointmentService.updateAppointmentStatus(
            id, 
            request.getStatus()
        );
        
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm an appointment.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.CONFIRMED);
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    /**
     * Start an appointment (mark as in progress).
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<AppointmentResponse> startAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.IN_PROGRESS);
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete an appointment.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.COMPLETED);
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an appointment.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        Appointment appointment = appointmentService.cancelAppointment(id, reason);
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark appointment as no-show.
     */
    @PostMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@PathVariable Long id) {
        Appointment appointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.NO_SHOW);
        AppointmentResponse response = mapToAppointmentResponse(appointment);
        return ResponseEntity.ok(response);
    }

    // === CALENDAR AND SCHEDULING ===

    /**
     * Get upcoming appointments.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments() {
        List<Appointment> appointments = appointmentService.findUpcomingAppointments();
        List<AppointmentResponse> response = appointments.stream()
            .map(this::mapToAppointmentResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get appointments by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AppointmentResponse>> getAppointmentsByStatus(
            @PathVariable AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointments = appointmentService.findAppointmentsByStatus(status, pageable);
        Page<AppointmentResponse> response = appointments.map(this::mapToAppointmentResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get technician's schedule.
     */
    @GetMapping("/technician/{technicianId}/schedule")
    public ResponseEntity<Page<AppointmentResponse>> getTechnicianSchedule(
            @PathVariable Long technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointments = appointmentService.findAppointmentsByTechnician(technicianId, pageable);
        Page<AppointmentResponse> response = appointments.map(this::mapToAppointmentResponse);
        return ResponseEntity.ok(response);
    }

    // === AVAILABILITY CHECKING ===

    /**
     * Check if technician is available during a time slot.
     */
    @GetMapping("/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        
        boolean available = appointmentService.isTechnicianAvailable(technicianId, startTime, endTime);
        return ResponseEntity.ok(available);
    }

    // === HELPER METHODS ===

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        TechnicianSummaryResponse technicianResponse = null;
        if (appointment.getTechnician() != null) {
            Technician technician = appointment.getTechnician();
            technicianResponse = new TechnicianSummaryResponse(
                technician.getId(),
                technician.getFullName(),
                technician.getEmail()
            );
        }
        
        AppointmentResponse.TicketSummaryResponse ticketResponse = null;
        if (appointment.getTicket() != null) {
            Ticket ticket = appointment.getTicket();
            ticketResponse = new AppointmentResponse.TicketSummaryResponse(
                ticket.getId(),
                ticket.getDescription(),
                ticket.getServiceType().toString(),
                ticket.getClient().getFullName(),
                ticket.getClient().getEmail()
            );
        }
        
        return new AppointmentResponse(
            appointment.getId(),
            technicianResponse,
            ticketResponse,
            appointment.getStartTime(),
            appointment.getEndTime(),
            appointment.getStatus(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt()
        );
    }
} 