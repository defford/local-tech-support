package com.localtechsupport.repository;

import com.localtechsupport.entity.Ticket;
import com.localtechsupport.entity.TicketStatus;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Technician;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for Ticket entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Status-based filtering (OPEN/CLOSED tickets)
 * - Client and technician association queries
 * - Service type filtering (HARDWARE/SOFTWARE)
 * - Time-based queries (created, due dates, overdue tickets)
 * - Multi-field searching with pagination
 * - Assignment management (unassigned tickets, workload distribution)
 * - Dashboard and reporting queries
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // JPQL query constants
    String SEARCH_TICKETS_QUERY = "SELECT t FROM Ticket t WHERE " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.client.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))";

    String FIND_OVERDUE_QUERY = "SELECT t FROM Ticket t WHERE " +
            "t.status = com.localtechsupport.entity.TicketStatus.OPEN AND t.dueAt < :currentTime";

    String FIND_DUE_SOON_QUERY = "SELECT t FROM Ticket t WHERE " +
            "t.status = com.localtechsupport.entity.TicketStatus.OPEN AND " +
            "t.dueAt BETWEEN :currentTime AND :thresholdTime";

    String COUNT_BY_STATUS_AND_SERVICE_QUERY = "SELECT COUNT(t) FROM Ticket t WHERE " +
            "t.status = :status AND t.serviceType = :serviceType";

    String COUNT_OVERDUE_QUERY = "SELECT COUNT(t) FROM Ticket t WHERE " +
            "t.status = com.localtechsupport.entity.TicketStatus.OPEN AND t.dueAt < :currentTime";

    // Status-based queries (paginated)
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    List<Ticket> findByStatus(TicketStatus status);

    // Client-based queries
    Page<Ticket> findByClient(Client client, Pageable pageable);
    
    List<Ticket> findByClient(Client client);
    
    Page<Ticket> findByClientAndStatus(Client client, TicketStatus status, Pageable pageable);

    // Technician assignment queries
    Page<Ticket> findByAssignedTechnician(Technician technician, Pageable pageable);
    
    List<Ticket> findByAssignedTechnician(Technician technician);
    
    Page<Ticket> findByAssignedTechnicianAndStatus(Technician technician, TicketStatus status, Pageable pageable);
    
    // Unassigned tickets
    Page<Ticket> findByAssignedTechnicianIsNull(Pageable pageable);
    
    List<Ticket> findByAssignedTechnicianIsNull();
    
    Page<Ticket> findByAssignedTechnicianIsNullAndStatus(TicketStatus status, Pageable pageable);

    // Service type queries
    Page<Ticket> findByServiceType(ServiceType serviceType, Pageable pageable);
    
    List<Ticket> findByServiceType(ServiceType serviceType);
    
    Page<Ticket> findByServiceTypeAndStatus(ServiceType serviceType, TicketStatus status, Pageable pageable);

    // Unassigned tickets by service type (for assignment optimization)
    Page<Ticket> findByServiceTypeAndAssignedTechnicianIsNull(ServiceType serviceType, Pageable pageable);
    
    List<Ticket> findByServiceTypeAndAssignedTechnicianIsNull(ServiceType serviceType);

    // Multi-field search with pagination
    @Query(SEARCH_TICKETS_QUERY)
    Page<Ticket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Time-based queries
    Page<Ticket> findByCreatedAtAfter(Instant dateTime, Pageable pageable);
    
    Page<Ticket> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    
    Page<Ticket> findByDueAtBefore(Instant dateTime, Pageable pageable);
    
    Page<Ticket> findByDueAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    // Overdue tickets
    @Query(FIND_OVERDUE_QUERY)
    Page<Ticket> findOverdueTickets(@Param("currentTime") Instant currentTime, Pageable pageable);
    
    @Query(FIND_OVERDUE_QUERY)
    List<Ticket> findOverdueTickets(@Param("currentTime") Instant currentTime);

    // Due soon tickets (for alerts/notifications)
    @Query(FIND_DUE_SOON_QUERY)
    Page<Ticket> findTicketsDueSoon(@Param("currentTime") Instant currentTime, 
                                   @Param("thresholdTime") Instant thresholdTime, 
                                   Pageable pageable);

    @Query(FIND_DUE_SOON_QUERY)
    List<Ticket> findTicketsDueSoon(@Param("currentTime") Instant currentTime, 
                                   @Param("thresholdTime") Instant thresholdTime);

    // Combined filtering queries
    Page<Ticket> findByStatusAndServiceType(TicketStatus status, ServiceType serviceType, Pageable pageable);
    
    Page<Ticket> findByStatusAndCreatedAtAfter(TicketStatus status, Instant dateTime, Pageable pageable);
    
    Page<Ticket> findByClientAndServiceType(Client client, ServiceType serviceType, Pageable pageable);

    // Count queries for dashboard/stats
    long countByStatus(TicketStatus status);
    
    long countByServiceType(ServiceType serviceType);
    
    @Query(COUNT_BY_STATUS_AND_SERVICE_QUERY)
    long countByStatusAndServiceType(@Param("status") TicketStatus status, 
                                    @Param("serviceType") ServiceType serviceType);
    
    long countByAssignedTechnician(Technician technician);
    
    long countByAssignedTechnicianIsNull();
    
    long countByClient(Client client);
    
    long countByCreatedAtAfter(Instant dateTime);
    
    @Query(COUNT_OVERDUE_QUERY)
    long countOverdueTickets(@Param("currentTime") Instant currentTime);

    // Workload distribution queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTechnician = :technician AND t.status = :status")
    long countByAssignedTechnicianAndStatus(@Param("technician") Technician technician, 
                                          @Param("status") TicketStatus status);
} 