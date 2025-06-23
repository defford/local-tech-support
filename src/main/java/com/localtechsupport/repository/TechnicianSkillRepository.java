package com.localtechsupport.repository;

import com.localtechsupport.entity.TechnicianSkill;
import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TechnicianSkill entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Technician-based skill management and queries
 * - Service type filtering and technician matching
 * - Skill assignment validation and duplicate prevention
 * - Technician qualification checking and verification
 * - Skills analytics and competency reporting
 * - Assignment optimization and workload distribution
 * - Skill gap analysis and training needs assessment
 */
@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, Long> {

    // JPQL query constants
    String FIND_TECHNICIANS_WITH_SKILL_QUERY = "SELECT DISTINCT ts.technician FROM TechnicianSkill ts WHERE ts.serviceType = :serviceType";

    String FIND_TECHNICIANS_WITH_MULTIPLE_SKILLS_QUERY = "SELECT ts.technician FROM TechnicianSkill ts WHERE ts.serviceType IN :serviceTypes GROUP BY ts.technician HAVING COUNT(DISTINCT ts.serviceType) = :skillCount";

    String SKILL_COVERAGE_QUERY = "SELECT ts.serviceType, COUNT(DISTINCT ts.technician) FROM TechnicianSkill ts GROUP BY ts.serviceType ORDER BY COUNT(DISTINCT ts.technician) DESC";

    String TECHNICIAN_SKILL_COUNT_QUERY = "SELECT ts.technician, COUNT(ts.serviceType) FROM TechnicianSkill ts GROUP BY ts.technician ORDER BY COUNT(ts.serviceType) DESC";

    String FIND_SKILLED_TECHNICIANS_COUNT_QUERY = "SELECT COUNT(DISTINCT ts.technician) FROM TechnicianSkill ts WHERE ts.serviceType = :serviceType";

    // Technician-based queries
    Page<TechnicianSkill> findByTechnician(Technician technician, Pageable pageable);
    
    List<TechnicianSkill> findByTechnician(Technician technician);

    // Service type-based queries
    Page<TechnicianSkill> findByServiceType(ServiceType serviceType, Pageable pageable);
    
    List<TechnicianSkill> findByServiceType(ServiceType serviceType);

    // Combination queries (specific skill assignments)
    Optional<TechnicianSkill> findByTechnicianAndServiceType(Technician technician, ServiceType serviceType);
    
    boolean existsByTechnicianAndServiceType(Technician technician, ServiceType serviceType);

    // Technician skill lists (for qualification checking)
    @Query("SELECT ts.serviceType FROM TechnicianSkill ts WHERE ts.technician = :technician")
    List<ServiceType> findServiceTypesByTechnician(@Param("technician") Technician technician);

    // Find technicians with specific skills
    @Query(FIND_TECHNICIANS_WITH_SKILL_QUERY)
    List<Technician> findTechniciansWithSkill(@Param("serviceType") ServiceType serviceType);

    @Query(FIND_TECHNICIANS_WITH_SKILL_QUERY)
    Page<Technician> findTechniciansWithSkill(@Param("serviceType") ServiceType serviceType, Pageable pageable);

    // Find technicians with multiple specific skills
    @Query(FIND_TECHNICIANS_WITH_MULTIPLE_SKILLS_QUERY)
    List<Technician> findTechniciansWithAllSkills(@Param("serviceTypes") List<ServiceType> serviceTypes,
                                                 @Param("skillCount") long skillCount);

    // Find technicians with any of the specified skills
    @Query("SELECT DISTINCT ts.technician FROM TechnicianSkill ts WHERE ts.serviceType IN :serviceTypes")
    List<Technician> findTechniciansWithAnySkill(@Param("serviceTypes") List<ServiceType> serviceTypes);

    @Query("SELECT DISTINCT ts.technician FROM TechnicianSkill ts WHERE ts.serviceType IN :serviceTypes")
    Page<Technician> findTechniciansWithAnySkill(@Param("serviceTypes") List<ServiceType> serviceTypes, Pageable pageable);

    // Skill validation and checking
    @Query("SELECT COUNT(ts) > 0 FROM TechnicianSkill ts WHERE ts.technician = :technician AND ts.serviceType = :serviceType")
    boolean isTechnicianQualifiedFor(@Param("technician") Technician technician, @Param("serviceType") ServiceType serviceType);

    // Count queries for dashboard/stats
    long countByTechnician(Technician technician);
    
    long countByServiceType(ServiceType serviceType);
    
    @Query(FIND_SKILLED_TECHNICIANS_COUNT_QUERY)
    long countTechniciansWithSkill(@Param("serviceType") ServiceType serviceType);

    // Skill distribution and analytics
    @Query(SKILL_COVERAGE_QUERY)
    List<Object[]> getSkillCoverageAnalysis();

    @Query(TECHNICIAN_SKILL_COUNT_QUERY)
    List<Object[]> getTechnicianSkillCounts();

    // Find technicians with no skills (training needs) - using native SQL to avoid Hibernate JPQL optimization issues
    @Query(value = "SELECT t.* FROM technicians t WHERE t.id NOT IN (SELECT DISTINCT ts.technician_id FROM technician_skills ts WHERE ts.technician_id IS NOT NULL)", nativeQuery = true)
    List<Technician> findTechniciansWithNoSkills();

    @Query(value = "SELECT t.* FROM technicians t WHERE t.id NOT IN (SELECT DISTINCT ts.technician_id FROM technician_skills ts WHERE ts.technician_id IS NOT NULL)", 
           countQuery = "SELECT COUNT(t.id) FROM technicians t WHERE t.id NOT IN (SELECT DISTINCT ts.technician_id FROM technician_skills ts WHERE ts.technician_id IS NOT NULL)",
           nativeQuery = true)
    Page<Technician> findTechniciansWithNoSkills(Pageable pageable);

    // Find technicians with only one skill (cross-training candidates)
    @Query("SELECT ts.technician FROM TechnicianSkill ts GROUP BY ts.technician HAVING COUNT(ts.serviceType) = 1")
    List<Technician> findTechniciansWithSingleSkill();

    // Find technicians with all available skills (versatile technicians)
    @Query("SELECT ts.technician FROM TechnicianSkill ts GROUP BY ts.technician HAVING COUNT(DISTINCT ts.serviceType) = (SELECT COUNT(DISTINCT st.serviceType) FROM TechnicianSkill st)")
    List<Technician> findFullyQualifiedTechnicians();

    // Skill gap analysis
    @Query("SELECT DISTINCT ts.serviceType FROM TechnicianSkill ts WHERE ts.serviceType NOT IN (SELECT DISTINCT ts2.serviceType FROM TechnicianSkill ts2 WHERE ts2.technician = :technician)")
    List<ServiceType> findMissingSkillsForTechnician(@Param("technician") Technician technician);

    // Find underrepresented skills (skills with few technicians)
    @Query("SELECT ts.serviceType FROM TechnicianSkill ts GROUP BY ts.serviceType HAVING COUNT(DISTINCT ts.technician) <= :maxTechnicians ORDER BY COUNT(DISTINCT ts.technician)")
    List<ServiceType> findUnderrepresentedSkills(@Param("maxTechnicians") long maxTechnicians);

    // Find overrepresented skills (skills with many technicians)
    @Query("SELECT ts.serviceType FROM TechnicianSkill ts GROUP BY ts.serviceType HAVING COUNT(DISTINCT ts.technician) >= :minTechnicians ORDER BY COUNT(DISTINCT ts.technician) DESC")
    List<ServiceType> findOverrepresentedSkills(@Param("minTechnicians") long minTechnicians);

    // Skill diversity analysis
    @Query("SELECT ts.technician, COUNT(DISTINCT ts.serviceType) as skillCount FROM TechnicianSkill ts GROUP BY ts.technician HAVING COUNT(DISTINCT ts.serviceType) >= :minSkills ORDER BY skillCount DESC")
    List<Object[]> findMostVersatileTechnicians(@Param("minSkills") long minSkills);

    // Team skill composition
    @Query("SELECT COUNT(DISTINCT ts.technician) as totalTechnicians, COUNT(DISTINCT ts.serviceType) as totalSkillTypes, COUNT(ts) as totalSkillAssignments FROM TechnicianSkill ts")
    List<Object[]> getSkillCompositionSummary();

    // Skill redundancy analysis (how many technicians per skill type)
    @Query("SELECT ts.serviceType, COUNT(DISTINCT ts.technician) as technicianCount FROM TechnicianSkill ts GROUP BY ts.serviceType ORDER BY technicianCount")
    List<Object[]> getSkillRedundancyAnalysis();

    // Find potential mentors (technicians with skills that others lack)
    @Query("SELECT ts1.technician, ts1.serviceType FROM TechnicianSkill ts1 WHERE EXISTS (SELECT t FROM Technician t WHERE NOT EXISTS (SELECT ts2 FROM TechnicianSkill ts2 WHERE ts2.technician = t AND ts2.serviceType = ts1.serviceType) AND t.status = com.localtechsupport.entity.TechnicianStatus.ACTIVE)")
    List<Object[]> findPotentialMentorsBySkill();

    // Training recommendations (skills needed by technicians)
    @Query("SELECT t, ts.serviceType FROM Technician t, TechnicianSkill ts WHERE t.status = com.localtechsupport.entity.TechnicianStatus.ACTIVE AND NOT EXISTS (SELECT ts2 FROM TechnicianSkill ts2 WHERE ts2.technician = t AND ts2.serviceType = ts.serviceType) ORDER BY t.id, ts.serviceType")
    List<Object[]> getTrainingRecommendations();

    // Skill assignment optimization (find best technician for service type)
    @Query("SELECT ts.technician FROM TechnicianSkill ts WHERE ts.serviceType = :serviceType AND ts.technician.status = com.localtechsupport.entity.TechnicianStatus.ACTIVE ORDER BY SIZE(ts.technician.assignedTickets)")
    List<Technician> findBestQualifiedTechnicians(@Param("serviceType") ServiceType serviceType);

    // Bulk operations support
    List<TechnicianSkill> findByTechnicianIn(List<Technician> technicians);
    
    List<TechnicianSkill> findByServiceTypeIn(List<ServiceType> serviceTypes);

    void deleteByTechnician(Technician technician);
    
    void deleteByTechnicianAndServiceType(Technician technician, ServiceType serviceType);

    // Check if skill assignment would create duplicate
    @Query("SELECT COUNT(ts) FROM TechnicianSkill ts WHERE ts.technician = :technician AND ts.serviceType = :serviceType")
    long countDuplicateSkills(@Param("technician") Technician technician, @Param("serviceType") ServiceType serviceType);
} 