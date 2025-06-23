package com.localtechsupport.repository;

import com.localtechsupport.entity.TicketHistory;
import com.localtechsupport.entity.Ticket;
import com.localtechsupport.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TicketHistory entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Ticket-based audit trail and change history
 * - Status change tracking and workflow analysis
 * - Time-based queries for historical reporting and trends
 * - User activity tracking and accountability (createdBy queries)
 * - Description content searching and change analysis
 * - Audit reporting and compliance queries
 * - Change pattern analysis and workflow optimization
 */
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    // JPQL query constants
    String SEARCH_DESCRIPTIONS_QUERY = "SELECT th FROM TicketHistory th WHERE " +
            "LOWER(th.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))";

    String FIND_STATUS_CHANGES_QUERY = "SELECT th FROM TicketHistory th WHERE " +
            "th.ticket = :ticket ORDER BY th.createdAt ASC";

    String FIND_RECENT_CHANGES_QUERY = "SELECT th FROM TicketHistory th WHERE " +
            "th.createdAt >= :sinceTime ORDER BY th.createdAt DESC";

    String FIND_USER_ACTIVITY_QUERY = "SELECT th FROM TicketHistory th WHERE " +
            "th.createdBy = :createdBy AND th.createdAt >= :sinceTime ORDER BY th.createdAt DESC";

    String COUNT_STATUS_CHANGES_QUERY = "SELECT COUNT(th) FROM TicketHistory th WHERE " +
            "th.ticket = :ticket AND th.status = :status";

    String FIND_WORKFLOW_PATTERN_QUERY = "SELECT th.status, COUNT(th) FROM TicketHistory th WHERE " +
            "th.createdAt >= :sinceTime GROUP BY th.status ORDER BY COUNT(th) DESC";

    // Ticket-based queries (audit trail)
    Page<TicketHistory> findByTicket(Ticket ticket, Pageable pageable);
    
    List<TicketHistory> findByTicket(Ticket ticket);
    
    // Ticket history ordered by time (complete audit trail)
    List<TicketHistory> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    
    List<TicketHistory> findByTicketOrderByCreatedAtDesc(Ticket ticket);
    
    // Latest/First history entries for a ticket
    Optional<TicketHistory> findTopByTicketOrderByCreatedAtDesc(Ticket ticket);
    
    Optional<TicketHistory> findTopByTicketOrderByCreatedAtAsc(Ticket ticket);

    // Status-based queries
    Page<TicketHistory> findByStatus(TicketStatus status, Pageable pageable);
    
    List<TicketHistory> findByStatus(TicketStatus status);
    
    // Ticket + Status combination (specific status changes for a ticket)
    Page<TicketHistory> findByTicketAndStatus(Ticket ticket, TicketStatus status, Pageable pageable);
    
    List<TicketHistory> findByTicketAndStatus(Ticket ticket, TicketStatus status);

    // User activity tracking (createdBy)
    Page<TicketHistory> findByCreatedBy(String createdBy, Pageable pageable);
    
    List<TicketHistory> findByCreatedBy(String createdBy);
    
    // User activity with status filter
    Page<TicketHistory> findByCreatedByAndStatus(String createdBy, TicketStatus status, Pageable pageable);
    
    List<TicketHistory> findByCreatedByAndStatus(String createdBy, TicketStatus status);

    // Time-based queries
    Page<TicketHistory> findByCreatedAtAfter(Instant dateTime, Pageable pageable);
    
    List<TicketHistory> findByCreatedAtAfter(Instant dateTime);
    
    Page<TicketHistory> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    
    List<TicketHistory> findByCreatedAtBetween(Instant startDate, Instant endDate);

    // Recent changes
    @Query(FIND_RECENT_CHANGES_QUERY)
    List<TicketHistory> findRecentChanges(@Param("sinceTime") Instant sinceTime);
    
    @Query(FIND_RECENT_CHANGES_QUERY)
    Page<TicketHistory> findRecentChanges(@Param("sinceTime") Instant sinceTime, Pageable pageable);

    // User activity in time range
    @Query(FIND_USER_ACTIVITY_QUERY)
    List<TicketHistory> findUserActivity(@Param("createdBy") String createdBy, 
                                        @Param("sinceTime") Instant sinceTime);

    @Query(FIND_USER_ACTIVITY_QUERY)
    Page<TicketHistory> findUserActivity(@Param("createdBy") String createdBy, 
                                        @Param("sinceTime") Instant sinceTime, 
                                        Pageable pageable);

    // Description content search
    @Query(SEARCH_DESCRIPTIONS_QUERY)
    Page<TicketHistory> searchByDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query(SEARCH_DESCRIPTIONS_QUERY)
    List<TicketHistory> searchByDescription(@Param("searchTerm") String searchTerm);

    // Combined filtering queries
    Page<TicketHistory> findByTicketAndCreatedBy(Ticket ticket, String createdBy, Pageable pageable);
    
    Page<TicketHistory> findByTicketAndCreatedAtAfter(Ticket ticket, Instant dateTime, Pageable pageable);
    
    Page<TicketHistory> findByStatusAndCreatedAtAfter(TicketStatus status, Instant dateTime, Pageable pageable);
    
    Page<TicketHistory> findByCreatedByAndCreatedAtBetween(String createdBy, 
                                                          Instant startDate, 
                                                          Instant endDate, 
                                                          Pageable pageable);

    // Ticket workflow analysis
    Page<TicketHistory> findByTicketAndStatusAndCreatedAtAfter(Ticket ticket, 
                                                              TicketStatus status, 
                                                              Instant dateTime, 
                                                              Pageable pageable);

    // Count queries for dashboard/stats
    long countByTicket(Ticket ticket);
    
    long countByStatus(TicketStatus status);
    
    long countByCreatedBy(String createdBy);
    
    long countByCreatedAtAfter(Instant dateTime);
    
    @Query(COUNT_STATUS_CHANGES_QUERY)
    long countStatusChangesForTicket(@Param("ticket") Ticket ticket, @Param("status") TicketStatus status);

    // Audit and compliance queries
    long countByTicketAndCreatedBy(Ticket ticket, String createdBy);
    
    long countByTicketAndStatus(Ticket ticket, TicketStatus status);
    
    long countByCreatedByAndCreatedAtAfter(String createdBy, Instant dateTime);

    // Workflow analysis queries
    @Query("SELECT th.status, COUNT(th) FROM TicketHistory th WHERE th.createdAt >= :sinceTime GROUP BY th.status ORDER BY COUNT(th) DESC")
    List<Object[]> getStatusChangeFrequency(@Param("sinceTime") Instant sinceTime);

    @Query("SELECT th.createdBy, COUNT(th) FROM TicketHistory th WHERE th.createdAt >= :sinceTime GROUP BY th.createdBy ORDER BY COUNT(th) DESC")
    List<Object[]> getUserActivitySummary(@Param("sinceTime") Instant sinceTime);

    @Query("SELECT DATE(th.createdAt), COUNT(th) FROM TicketHistory th WHERE th.createdAt >= :sinceTime GROUP BY DATE(th.createdAt) ORDER BY DATE(th.createdAt)")
    List<Object[]> getDailyChangeActivity(@Param("sinceTime") Instant sinceTime);

    // Ticket lifecycle analysis
    @Query("SELECT t, COUNT(th) FROM TicketHistory th JOIN th.ticket t GROUP BY t HAVING COUNT(th) >= :minChanges ORDER BY COUNT(th) DESC")
    List<Object[]> getTicketsWithMostChanges(@Param("minChanges") long minChanges);

    @Query("SELECT t, MIN(th.createdAt), MAX(th.createdAt) FROM TicketHistory th JOIN th.ticket t GROUP BY t")
    List<Object[]> getTicketLifecycleSpans();

    // Status transition analysis
    @Query("SELECT th1.status, th2.status, COUNT(*) FROM TicketHistory th1 JOIN TicketHistory th2 ON th1.ticket = th2.ticket WHERE th2.createdAt > th1.createdAt AND NOT EXISTS (SELECT th3 FROM TicketHistory th3 WHERE th3.ticket = th1.ticket AND th3.createdAt > th1.createdAt AND th3.createdAt < th2.createdAt) GROUP BY th1.status, th2.status ORDER BY COUNT(*) DESC")
    List<Object[]> getStatusTransitionPatterns();

    // Performance and efficiency queries
    @Query("SELECT th.createdBy, th.status, COUNT(th) FROM TicketHistory th WHERE th.createdAt >= :sinceTime GROUP BY th.createdBy, th.status ORDER BY th.createdBy")
    List<Object[]> getUserPerformanceByStatus(@Param("sinceTime") Instant sinceTime);

    // Recent activity by ticket
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket = :ticket AND th.createdAt >= :sinceTime ORDER BY th.createdAt DESC")
    List<TicketHistory> findRecentActivityForTicket(@Param("ticket") Ticket ticket, 
                                                   @Param("sinceTime") Instant sinceTime);

    // Find tickets with no recent activity (stale tickets)
    @Query("SELECT DISTINCT t FROM Ticket t WHERE NOT EXISTS (SELECT th FROM TicketHistory th WHERE th.ticket = t AND th.createdAt >= :sinceTime)")
    List<Ticket> findTicketsWithNoRecentActivity(@Param("sinceTime") Instant sinceTime);

    // Bulk status change tracking
    @Query("SELECT th FROM TicketHistory th WHERE th.createdBy = :createdBy AND th.status = :status AND th.createdAt BETWEEN :startTime AND :endTime ORDER BY th.createdAt")
    List<TicketHistory> findBulkStatusChanges(@Param("createdBy") String createdBy,
                                            @Param("status") TicketStatus status,
                                            @Param("startTime") Instant startTime,
                                            @Param("endTime") Instant endTime);
} 