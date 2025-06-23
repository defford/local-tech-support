package com.localtechsupport.repository;

import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Client.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for Client entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Email-based lookups (unique constraint)
 * - Status-based filtering 
 * - Multi-field searching with pagination
 * - Time-based queries for reporting
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // JPQL query constants
    String SEARCH_CLIENTS_QUERY = "SELECT c FROM Client c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))";

    String DATE_RANGE_QUERY = "SELECT c FROM Client c WHERE c.createdAt BETWEEN :startDate AND :endDate";

    String COUNT_BY_DATE_QUERY = "SELECT COUNT(c) FROM Client c WHERE c.createdAt >= :dateTime";

    // Unique lookups
    Optional<Client> findByEmail(String email);
    
    boolean existsByEmail(String email);

    // Status-based queries (paginated)
    Page<Client> findByStatus(ClientStatus status, Pageable pageable);
    
    // Multi-field search with pagination
    @Query(SEARCH_CLIENTS_QUERY)
    Page<Client> searchClients(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Time-based queries (paginated)
    Page<Client> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);
    
    @Query(DATE_RANGE_QUERY)
    Page<Client> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);

    // Combined filtering (paginated)
    Page<Client> findByStatusAndCreatedAtAfter(ClientStatus status, LocalDateTime dateTime, Pageable pageable);
    
    // Count queries for dashboard/stats
    long countByStatus(ClientStatus status);
    
    @Query(COUNT_BY_DATE_QUERY)
    long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);
} 