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
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Ticket management operations.
 * 
 * Handles core business logic for:
 * - Ticket creation with validation and history tracking
 * - Technician assignment with workload balancing
 * - Status management with proper transitions
 * - Search and filtering operations
 * - Overdue ticket monitoring
 * - Assignment optimization
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                        ClientRepository clientRepository,
                        TechnicianRepository technicianRepository,
                        TicketHistoryRepository ticketHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.technicianRepository = technicianRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    /**
     * Creates a new support ticket with validation and history tracking.
     */
    public Ticket createTicket(Long clientId, ServiceType serviceType, String description) {
        // Validate client exists and is active
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + clientId));
        
        if (client.getStatus() != Client.ClientStatus.ACTIVE) {
            throw new IllegalStateException("Cannot create ticket for inactive client: " + client.getEmail());
        }

        // Create ticket with auto-calculated due date
        Ticket ticket = new Ticket();
        ticket.setClient(client);
        ticket.setServiceType(serviceType);
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setDueAt(calculateDueDate(serviceType));
        
        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create initial history entry
        createHistoryEntry(savedTicket, TicketStatus.OPEN, 
            "Ticket created - " + serviceType + " support request", "SYSTEM");
        
        return savedTicket;
    }

    /**
     * Assigns a technician to a ticket with validation and history tracking.
     */
    public Ticket assignTechnician(Long ticketId, Long technicianId) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Cannot assign technician to closed ticket");
        }

        Technician technician = technicianRepository.findById(technicianId)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));
        
        if (technician.getStatus() != TechnicianStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign inactive technician: " + technician.getEmail());
        }

        // Validate technician can handle this service type (basic validation)
        if (!canTechnicianHandleServiceType(technician, ticket.getServiceType())) {
            throw new IllegalStateException("Technician does not have skills for " + ticket.getServiceType() + " tickets");
        }

        // Assign technician
        ticket.setAssignedTechnician(technician);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create history entry
        createHistoryEntry(savedTicket, TicketStatus.OPEN, 
            "Assigned to technician: " + technician.getFullName(), "SYSTEM");
        
        return savedTicket;
    }

    /**
     * Updates ticket status with validation and history tracking.
     */
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus, String reason, String updatedBy) {
        Ticket ticket = getTicketById(ticketId);
        
        if (!isValidStatusTransition(ticket.getStatus(), newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + ticket.getStatus() + " to " + newStatus);
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create history entry
        String historyDescription = String.format("Status changed from %s to %s", oldStatus, newStatus);
        if (reason != null && !reason.trim().isEmpty()) {
            historyDescription += " - " + reason;
        }
        
        createHistoryEntry(savedTicket, newStatus, historyDescription, updatedBy);
        
        return savedTicket;
    }

    /**
     * Closes a ticket with resolution details.
     */
    public Ticket closeTicket(Long ticketId, String resolutionNotes, String closedBy) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Cannot close ticket that is not open");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create history entry with resolution
        String historyDescription = "Ticket closed";
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            historyDescription += " - Resolution: " + resolutionNotes;
        }
        
        createHistoryEntry(savedTicket, TicketStatus.CLOSED, historyDescription, closedBy);
        
        return savedTicket;
    }

    /**
     * Removes technician assignment from a ticket.
     */
    public Ticket unassignTechnician(Long ticketId, String reason, String updatedBy) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getAssignedTechnician() == null) {
            throw new IllegalStateException("Ticket is not currently assigned to any technician");
        }

        String technicianName = ticket.getAssignedTechnician().getFullName();
        ticket.setAssignedTechnician(null);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create history entry
        String historyDescription = "Unassigned from technician: " + technicianName;
        if (reason != null && !reason.trim().isEmpty()) {
            historyDescription += " - " + reason;
        }
        
        createHistoryEntry(savedTicket, ticket.getStatus(), historyDescription, updatedBy);
        
        return savedTicket;
    }

    // === SEARCH AND RETRIEVAL METHODS ===

    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByClient(Long clientId, Pageable pageable) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + clientId));
        return ticketRepository.findByClient(client, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByTechnician(Long technicianId, Pageable pageable) {
        Technician technician = technicianRepository.findById(technicianId)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));
        return ticketRepository.findByAssignedTechnician(technician, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findUnassignedTickets(Pageable pageable) {
        return ticketRepository.findByAssignedTechnicianIsNullAndStatus(TicketStatus.OPEN, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> searchTickets(String searchTerm, Pageable pageable) {
        return ticketRepository.searchTickets(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findOverdueTickets() {
        return ticketRepository.findOverdueTickets(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTicketsDueSoon(int hoursThreshold) {
        Instant now = Instant.now();
        Instant threshold = now.plus(hoursThreshold, ChronoUnit.HOURS);
        return ticketRepository.findTicketsDueSoon(now, threshold);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByServiceType(ServiceType serviceType, Pageable pageable) {
        return ticketRepository.findByServiceType(serviceType, pageable);
    }

    // === ASSIGNMENT OPTIMIZATION ===

    /**
     * Finds the best available technician for a ticket based on workload and skills.
     */
    @Transactional(readOnly = true)
    public Optional<Technician> findBestTechnicianForTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return findBestTechnicianForServiceType(ticket.getServiceType());
    }

    /**
     * Finds the best available technician for a service type.
     */
    @Transactional(readOnly = true)
    public Optional<Technician> findBestTechnicianForServiceType(ServiceType serviceType) {
        // Get all active technicians
        List<Technician> availableTechnicians = technicianRepository.findByStatus(TechnicianStatus.ACTIVE);
        
        if (availableTechnicians.isEmpty()) {
            return Optional.empty();
        }

        // Filter by service type capability and find least loaded
        return availableTechnicians.stream()
            .filter(tech -> canTechnicianHandleServiceType(tech, serviceType))
            .min((t1, t2) -> {
                long workload1 = ticketRepository.countByAssignedTechnicianAndStatus(t1, TicketStatus.OPEN);
                long workload2 = ticketRepository.countByAssignedTechnicianAndStatus(t2, TicketStatus.OPEN);
                return Long.compare(workload1, workload2);
            });
    }

    // === STATISTICS AND REPORTING ===

    @Transactional(readOnly = true)
    public long countTicketsByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countOverdueTickets() {
        return ticketRepository.countOverdueTickets(Instant.now());
    }

    @Transactional(readOnly = true)
    public long countUnassignedTickets() {
        return ticketRepository.countByAssignedTechnicianIsNull();
    }

    @Transactional(readOnly = true)
    public long countTicketsByServiceType(ServiceType serviceType) {
        return ticketRepository.countByServiceType(serviceType);
    }

    // === PRIVATE HELPER METHODS ===

    private void createHistoryEntry(Ticket ticket, TicketStatus status, String description, String createdBy) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus(status);
        history.setDescription(description);
        history.setCreatedBy(createdBy);
        ticketHistoryRepository.save(history);
    }

    private Instant calculateDueDate(ServiceType serviceType) {
        // Business rule: Hardware issues get 24 hours, Software gets 48 hours
        int hours = (serviceType == ServiceType.HARDWARE) ? 24 : 48;
        return Instant.now().plus(hours, ChronoUnit.HOURS);
    }

    private boolean isValidStatusTransition(TicketStatus from, TicketStatus to) {
        // Simple validation: Can only go from OPEN to CLOSED or stay the same
        return from == to || (from == TicketStatus.OPEN && to == TicketStatus.CLOSED);
    }

    private boolean canTechnicianHandleServiceType(Technician technician, ServiceType serviceType) {
        // For now, assume all active technicians can handle both types
        // This will be enhanced when TechnicianSkill relationships are fully implemented
        return technician.getStatus() == TechnicianStatus.ACTIVE;
    }
} 