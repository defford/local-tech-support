package com.localtechsupport.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TechnicianSkill Entity Tests")
class TechnicianSkillTest {

    @Mock
    private Technician mockTechnician;
    
    @Mock
    private ServiceType mockServiceType;

    private TechnicianSkill technicianSkill;

    @BeforeEach
    void setUp() {
        technicianSkill = new TechnicianSkill();
        
        // Set up basic technician skill properties
        technicianSkill.setTechnician(mockTechnician);
        technicianSkill.setServiceType(mockServiceType);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create technician skill with default constructor")
        void shouldCreateTechnicianSkillWithDefaultConstructor() {
            TechnicianSkill newTechnicianSkill = new TechnicianSkill();
            
            assertNotNull(newTechnicianSkill);
            assertNull(newTechnicianSkill.getId());
            assertNull(newTechnicianSkill.getTechnician());
            assertNull(newTechnicianSkill.getServiceType());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            technicianSkill.setId(1L);
            
            assertEquals(1L, technicianSkill.getId());
        }

        @Test
        @DisplayName("Should set and get technician correctly")
        void shouldSetAndGetTechnicianCorrectly() {
            Technician newTechnician = mock(Technician.class);
            technicianSkill.setTechnician(newTechnician);
            
            assertEquals(newTechnician, technicianSkill.getTechnician());
        }

        @Test
        @DisplayName("Should set and get service type correctly")
        void shouldSetAndGetServiceTypeCorrectly() {
            ServiceType newServiceType = mock(ServiceType.class);
            technicianSkill.setServiceType(newServiceType);
            
            assertEquals(newServiceType, technicianSkill.getServiceType());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain technician relationship")
        void shouldMaintainTechnicianRelationship() {
            assertNotNull(technicianSkill.getTechnician());
            assertEquals(mockTechnician, technicianSkill.getTechnician());
        }

        @Test
        @DisplayName("Should maintain service type relationship")
        void shouldMaintainServiceTypeRelationship() {
            assertNotNull(technicianSkill.getServiceType());
            assertEquals(mockServiceType, technicianSkill.getServiceType());
        }

        @Test
        @DisplayName("Should handle null technician")
        void shouldHandleNullTechnician() {
            assertDoesNotThrow(() -> {
                technicianSkill.setTechnician(null);
                assertNull(technicianSkill.getTechnician());
            });
        }

        @Test
        @DisplayName("Should handle null service type")
        void shouldHandleNullServiceType() {
            assertDoesNotThrow(() -> {
                technicianSkill.setServiceType(null);
                assertNull(technicianSkill.getServiceType());
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle complete skill record")
        void shouldHandleCompleteSkillRecord() {
            assertAll(
                () -> assertNotNull(technicianSkill.getTechnician()),
                () -> assertNotNull(technicianSkill.getServiceType())
            );
        }

        @Test
        @DisplayName("Should handle empty skill record")
        void shouldHandleEmptySkillRecord() {
            TechnicianSkill emptySkill = new TechnicianSkill();
            
            assertAll(
                () -> assertNull(emptySkill.getId()),
                () -> assertNull(emptySkill.getTechnician()),
                () -> assertNull(emptySkill.getServiceType())
            );
        }
    }
} 