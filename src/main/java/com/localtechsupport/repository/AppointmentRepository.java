package com.localtechsupport.repository;

import com.localtechsupport.entity.Appointment;
import com.localtechsupport.entity.AppointmentStatus;
import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for Appointment entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Status-based filtering (appointment workflow management)
 * - Technician scheduling and workload queries
 * - Ticket-based appointment tracking
 * - Time-based queries (calendar views, scheduling conflicts)
 * - Scheduling conflict detection and prevention
 * - Calendar and availability management
 * - Dashboard and reporting queries
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // JPQL query constants
    String FIND_CONFLICTS_QUERY = "SELECT a FROM Appointment a WHERE " +
            "a.technician = :technician AND a.status NOT IN (:excludedStatuses) AND " +
            "((a.startTime <= :endTime AND a.endTime >= :startTime))";

    String FIND_BY_TIME_RANGE_QUERY = "SELECT a FROM Appointment a WHERE " +
            "a.startTime >= :startTime AND a.endTime <= :endTime";

    String FIND_TECHNICIAN_SCHEDULE_QUERY = "SELECT a FROM Appointment a WHERE " +
            "a.technician = :technician AND a.startTime >= :startDate AND a.startTime < :endDate";

    String COUNT_BY_STATUS_AND_TECHNICIAN_QUERY = "SELECT COUNT(a) FROM Appointment a WHERE " +
            "a.status = :status AND a.technician = :technician";

    String FIND_UPCOMING_QUERY = "SELECT a FROM Appointment a WHERE " +
            "a.startTime >= :currentTime AND a.status IN (:activeStatuses)";

    // Status-based queries (paginated)
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    
    List<Appointment> findByStatus(AppointmentStatus status);

    // Technician-based queries
    Page<Appointment> findByTechnician(Technician technician, Pageable pageable);
    
    List<Appointment> findByTechnician(Technician technician);
    
    Page<Appointment> findByTechnicianAndStatus(Technician technician, AppointmentStatus status, Pageable pageable);
    
    List<Appointment> findByTechnicianAndStatus(Technician technician, AppointmentStatus status);

    // Ticket-based queries
    Page<Appointment> findByTicket(Ticket ticket, Pageable pageable);
    
    List<Appointment> findByTicket(Ticket ticket);
    
    Page<Appointment> findByTicketAndStatus(Ticket ticket, AppointmentStatus status, Pageable pageable);

    // Time-based queries for scheduling
    Page<Appointment> findByStartTimeBetween(Instant startTime, Instant endTime, Pageable pageable);
    
    List<Appointment> findByStartTimeBetween(Instant startTime, Instant endTime);
    
    Page<Appointment> findByEndTimeBetween(Instant startTime, Instant endTime, Pageable pageable);

    // Technician schedule queries
    @Query(FIND_TECHNICIAN_SCHEDULE_QUERY)
    List<Appointment> findTechnicianSchedule(@Param("technician") Technician technician,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);

    @Query(FIND_TECHNICIAN_SCHEDULE_QUERY)
    Page<Appointment> findTechnicianSchedule(@Param("technician") Technician technician,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           Pageable pageable);

    // Calendar and time range queries
    @Query(FIND_BY_TIME_RANGE_QUERY)
    List<Appointment> findByTimeRange(@Param("startTime") Instant startTime,
                                    @Param("endTime") Instant endTime);

    @Query(FIND_BY_TIME_RANGE_QUERY)
    Page<Appointment> findByTimeRange(@Param("startTime") Instant startTime,
                                    @Param("endTime") Instant endTime,
                                    Pageable pageable);

    // Conflict detection queries
    @Query(FIND_CONFLICTS_QUERY)
    List<Appointment> findConflictingAppointments(@Param("technician") Technician technician,
                                                 @Param("startTime") Instant startTime,
                                                 @Param("endTime") Instant endTime,
                                                 @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses);

    // Check if technician is available during time slot
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
           "a.technician = :technician AND a.status NOT IN (:excludedStatuses) AND " +
           "((a.startTime <= :endTime AND a.endTime >= :startTime))")
    long countConflictingAppointments(@Param("technician") Technician technician,
                                    @Param("startTime") Instant startTime,
                                    @Param("endTime") Instant endTime,
                                    @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses);

    // Upcoming appointments
    @Query(FIND_UPCOMING_QUERY)
    List<Appointment> findUpcomingAppointments(@Param("currentTime") Instant currentTime,
                                             @Param("activeStatuses") List<AppointmentStatus> activeStatuses);

    @Query(FIND_UPCOMING_QUERY)
    Page<Appointment> findUpcomingAppointments(@Param("currentTime") Instant currentTime,
                                             @Param("activeStatuses") List<AppointmentStatus> activeStatuses,
                                             Pageable pageable);

    // Technician's upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.technician = :technician AND a.startTime >= :currentTime AND a.status IN (:activeStatuses)")
    List<Appointment> findTechnicianUpcomingAppointments(@Param("technician") Technician technician,
                                                       @Param("currentTime") Instant currentTime,
                                                       @Param("activeStatuses") List<AppointmentStatus> activeStatuses);

    // Overdue/missed appointments
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.endTime < :currentTime AND a.status = com.localtechsupport.entity.AppointmentStatus.CONFIRMED")
    List<Appointment> findMissedAppointments(@Param("currentTime") Instant currentTime);

    // Combined filtering queries
    Page<Appointment> findByTechnicianAndStartTimeBetween(Technician technician, 
                                                        Instant startTime, 
                                                        Instant endTime, 
                                                        Pageable pageable);

    Page<Appointment> findByStatusAndStartTimeBetween(AppointmentStatus status,
                                                    Instant startTime,
                                                    Instant endTime,
                                                    Pageable pageable);

    // Count queries for dashboard/stats
    long countByStatus(AppointmentStatus status);
    
    long countByTechnician(Technician technician);
    
    @Query(COUNT_BY_STATUS_AND_TECHNICIAN_QUERY)
    long countByStatusAndTechnician(@Param("status") AppointmentStatus status,
                                  @Param("technician") Technician technician);

    long countByTicket(Ticket ticket);

    // Time-based counts
    long countByStartTimeBetween(Instant startTime, Instant endTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
           "a.startTime >= :currentTime AND a.status IN (:activeStatuses)")
    long countUpcomingAppointments(@Param("currentTime") Instant currentTime,
                                 @Param("activeStatuses") List<AppointmentStatus> activeStatuses);

    // Workload and availability queries
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
           "a.technician = :technician AND a.startTime >= :startTime AND a.startTime < :endTime AND " +
           "a.status IN (:activeStatuses)")
    long countTechnicianAppointmentsInPeriod(@Param("technician") Technician technician,
                                           @Param("startTime") Instant startTime,
                                           @Param("endTime") Instant endTime,
                                           @Param("activeStatuses") List<AppointmentStatus> activeStatuses);
} 