package com.localtechsupport.repository;

import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianStatus;
import com.localtechsupport.entity.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Technician entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Email-based lookups (unique constraint)
 * - Status-based filtering 
 * - Multi-field searching with pagination
 * - Skills and service type matching
 * - Load balancing and availability queries
 * - Combined filtering for assignment optimization
 */
@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    // JPQL query constants
    String SEARCH_TECHNICIANS_QUERY = "SELECT t FROM Technician t WHERE " +
            "LOWER(t.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))";

    String FIND_BY_SERVICE_TYPE_QUERY = "SELECT DISTINCT t FROM Technician t " +
            "JOIN t.skills s WHERE s.serviceType = :serviceType";

    String FIND_ACTIVE_BY_SERVICE_TYPE_QUERY = "SELECT DISTINCT t FROM Technician t " +
            "JOIN t.skills s WHERE t.status = :status AND s.serviceType = :serviceType";

    String FIND_BY_MAX_LOAD_QUERY = "SELECT t FROM Technician t WHERE t.status = :status AND " +
            "SIZE(t.assignedTickets) <= :maxLoad";

    String COUNT_BY_SERVICE_TYPE_QUERY = "SELECT COUNT(DISTINCT t) FROM Technician t " +
            "JOIN t.skills s WHERE s.serviceType = :serviceType";

    // Unique lookups
    Optional<Technician> findByEmail(String email);
    
    boolean existsByEmail(String email);

    // Status-based queries (paginated)
    Page<Technician> findByStatus(TechnicianStatus status, Pageable pageable);
    
    List<Technician> findByStatus(TechnicianStatus status);
    
    // Multi-field search with pagination
    @Query(SEARCH_TECHNICIANS_QUERY)
    Page<Technician> searchTechnicians(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Skills and service type queries
    @Query(FIND_BY_SERVICE_TYPE_QUERY)
    Page<Technician> findByServiceType(@Param("serviceType") ServiceType serviceType, Pageable pageable);
    
    @Query(FIND_BY_SERVICE_TYPE_QUERY)
    List<Technician> findByServiceType(@Param("serviceType") ServiceType serviceType);

    // Combined status and skills queries
    @Query(FIND_ACTIVE_BY_SERVICE_TYPE_QUERY)
    Page<Technician> findByStatusAndServiceType(@Param("status") TechnicianStatus status, 
                                               @Param("serviceType") ServiceType serviceType, 
                                               Pageable pageable);
    
    @Query(FIND_ACTIVE_BY_SERVICE_TYPE_QUERY)
    List<Technician> findByStatusAndServiceType(@Param("status") TechnicianStatus status, 
                                               @Param("serviceType") ServiceType serviceType);

    // Load balancing queries
    @Query(FIND_BY_MAX_LOAD_QUERY)
    List<Technician> findByStatusAndCurrentLoadLessThanEqual(@Param("status") TechnicianStatus status, 
                                                            @Param("maxLoad") long maxLoad);

    // Available technicians (active status with manageable load)
    @Query("SELECT t FROM Technician t WHERE t.status = :activeStatus AND " +
           "SIZE(t.assignedTickets) < :maxLoad")
    List<Technician> findAvailableTechnicians(@Param("activeStatus") TechnicianStatus activeStatus,
                                             @Param("maxLoad") long maxLoad);

    // Available technicians for specific service type
    @Query("SELECT DISTINCT t FROM Technician t JOIN t.skills s WHERE " +
           "t.status = :activeStatus AND s.serviceType = :serviceType AND " +
           "SIZE(t.assignedTickets) < :maxLoad")
    List<Technician> findAvailableTechniciansForService(@Param("activeStatus") TechnicianStatus activeStatus,
                                                       @Param("serviceType") ServiceType serviceType, 
                                                       @Param("maxLoad") long maxLoad);

    // Count queries for dashboard/stats
    long countByStatus(TechnicianStatus status);
    
    @Query(COUNT_BY_SERVICE_TYPE_QUERY)
    long countByServiceType(@Param("serviceType") ServiceType serviceType);

    @Query("SELECT COUNT(t) FROM Technician t WHERE t.status = :activeStatus AND " +
           "SIZE(t.assignedTickets) < :maxLoad")
    long countAvailableTechnicians(@Param("activeStatus") TechnicianStatus activeStatus,
                                  @Param("maxLoad") long maxLoad);
} 