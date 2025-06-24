package com.localtechsupport.service;

import com.localtechsupport.entity.*;
import com.localtechsupport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Appointment management operations.
 * 
 * Handles core business logic for:
 * - Appointment scheduling with conflict detection
 * - Technician availability validation
 * - Status management and workflow transitions
 * - Calendar and time slot management
 * - Statistics and utilization analytics
 * - Business rule enforcement
 */
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TechnicianRepository technicianRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                            TechnicianRepository technicianRepository,
                            TicketRepository ticketRepository) {
        this.appointmentRepository = appointmentRepository;
        this.technicianRepository = technicianRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Creates a new appointment with conflict detection and validation.
     */
    public Appointment createAppointment(Long technicianId, Long ticketId, 
                                       Instant startTime, Instant endTime) {
        // Validate basic inputs
        validateAppointmentTimes(startTime, endTime);
        
        // Validate technician exists and is available
        Technician technician = getTechnicianById(technicianId);
        validateTechnicianAvailability(technician);
        
        // Validate ticket exists and can have appointments
        Ticket ticket = getTicketById(ticketId);
        validateTicketForAppointment(ticket);
        
        // Check for scheduling conflicts
        List<AppointmentStatus> excludedStatuses = Arrays.asList(
            AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
        
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
            technician, startTime, endTime, excludedStatuses);
            
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                "Technician " + technician.getFullName() + 
                " has conflicting appointments during the requested time slot");
        }
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setTechnician(technician);
        appointment.setTicket(ticket);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        return appointmentRepository.save(appointment);
    }

    /**
     * Updates appointment status with business rule validation.
     */
    public Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (!isValidStatusTransition(appointment.getStatus(), newStatus)) {
            throw new IllegalStateException(
                "Invalid status transition from " + appointment.getStatus() + " to " + newStatus);
        }
        
        appointment.setStatus(newStatus);
        return appointmentRepository.save(appointment);
    }

    /**
     * Cancels an appointment.
     */
    public Appointment cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }
        
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    // === SEARCH AND RETRIEVAL METHODS ===

    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAppointmentsByTechnician(Long technicianId, Pageable pageable) {
        Technician technician = getTechnicianById(technicianId);
        return appointmentRepository.findByTechnician(technician, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAppointmentsByTicket(Long ticketId, Pageable pageable) {
        Ticket ticket = getTicketById(ticketId);
        return appointmentRepository.findByTicket(ticket, pageable);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findTechnicianSchedule(Long technicianId, Instant startDate, Instant endDate) {
        Technician technician = getTechnicianById(technicianId);
        return appointmentRepository.findTechnicianSchedule(technician, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByTimeRange(Instant startTime, Instant endTime) {
        return appointmentRepository.findByTimeRange(startTime, endTime);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findUpcomingAppointments() {
        List<AppointmentStatus> activeStatuses = Arrays.asList(
            AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS);
        return appointmentRepository.findUpcomingAppointments(Instant.now(), activeStatuses);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findMissedAppointments() {
        return appointmentRepository.findMissedAppointments(Instant.now());
    }

    // === AVAILABILITY AND CONFLICT CHECKING ===

    @Transactional(readOnly = true)
    public boolean isTechnicianAvailable(Long technicianId, Instant startTime, Instant endTime) {
        Technician technician = getTechnicianById(technicianId);
        List<AppointmentStatus> excludedStatuses = Arrays.asList(
            AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
            
        long conflicts = appointmentRepository.countConflictingAppointments(
            technician, startTime, endTime, excludedStatuses);
            
        return conflicts == 0;
    }

    @Transactional(readOnly = true)
    public List<Instant> findAvailableTimeSlots(Long technicianId, Instant date, int durationMinutes) {
        // This would implement logic to find available time slots for a technician on a given date
        // For now, returning empty list as this would require more complex scheduling algorithm
        return new ArrayList<>();
    }

    // === STATISTICS AND ANALYTICS ===

    @Transactional(readOnly = true)
    public long countAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countUpcomingAppointments() {
        List<AppointmentStatus> activeStatuses = Arrays.asList(
            AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS);
        return appointmentRepository.countUpcomingAppointments(Instant.now(), activeStatuses);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getAppointmentCountsByTechnician() {
        List<Technician> technicians = technicianRepository.findAll();
        return technicians.stream()
            .collect(Collectors.toMap(
                Technician::getId,
                tech -> appointmentRepository.countByTechnician(tech)
            ));
    }

    // === PRIVATE HELPER METHODS ===

    private void validateAppointmentTimes(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        if (startTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }
        
        // Business rule: appointments must be at least 30 minutes
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (durationMinutes < 30) {
            throw new IllegalArgumentException("Appointments must be at least 30 minutes long");
        }
        
        // Business rule: appointments cannot be longer than 8 hours
        if (durationMinutes > 480) {
            throw new IllegalArgumentException("Appointments cannot be longer than 8 hours");
        }
    }

    private void validateTechnicianAvailability(Technician technician) {
        if (technician.getStatus() != TechnicianStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule appointments with inactive technician: " + 
                technician.getFullName());
        }
    }

    private void validateTicketForAppointment(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Cannot schedule appointments for closed tickets");
        }
    }

    private boolean isValidStatusTransition(AppointmentStatus from, AppointmentStatus to) {
        switch (from) {
            case PENDING:
                return to == AppointmentStatus.CONFIRMED || 
                       to == AppointmentStatus.CANCELLED;
            case CONFIRMED:
                return to == AppointmentStatus.IN_PROGRESS || 
                       to == AppointmentStatus.CANCELLED ||
                       to == AppointmentStatus.NO_SHOW;
            case IN_PROGRESS:
                return to == AppointmentStatus.COMPLETED;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                return false; // Terminal states
            default:
                return false;
        }
    }

    private Technician getTechnicianById(Long technicianId) {
        return technicianRepository.findById(technicianId)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));
    }

    private Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
    }
} 