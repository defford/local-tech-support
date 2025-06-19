package com.localtechsupport.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Technician Entity Tests")
class TechnicianTest {

    private Technician technician;

    @BeforeEach
    void setUp() {
        technician = new Technician();
        technician.setFullName("John Smith");
        technician.setEmail("john.smith@techsupport.com");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create technician with default constructor")
        void shouldCreateTechnicianWithDefaultConstructor() {
            Technician newTechnician = new Technician();
            
            assertNotNull(newTechnician);
            assertNull(newTechnician.getId());
            assertEquals(TechnicianStatus.ACTIVE, newTechnician.getStatus());
            assertNotNull(newTechnician.getSkills());
            assertNotNull(newTechnician.getAssignedTickets());
            assertNotNull(newTechnician.getAppointments());
            assertTrue(newTechnician.getSkills().isEmpty());
            assertTrue(newTechnician.getAssignedTickets().isEmpty());
            assertTrue(newTechnician.getAppointments().isEmpty());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            technician.setId(1L);
            
            assertEquals(1L, technician.getId());
        }

        @Test
        @DisplayName("Should set and get full name correctly")
        void shouldSetAndGetFullNameCorrectly() {
            technician.setFullName("Jane Doe");
            
            assertEquals("Jane Doe", technician.getFullName());
        }

        @Test
        @DisplayName("Should set and get email correctly")
        void shouldSetAndGetEmailCorrectly() {
            technician.setEmail("jane.doe@example.com");
            
            assertEquals("jane.doe@example.com", technician.getEmail());
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatusCorrectly() {
            technician.setStatus(TechnicianStatus.IN_TRAINING);
            
            assertEquals(TechnicianStatus.IN_TRAINING, technician.getStatus());
        }

        @Test
        @DisplayName("Should set and get skills correctly")
        void shouldSetAndGetSkillsCorrectly() {
            Set<TechnicianSkill> skills = new HashSet<>();
            // Note: TechnicianSkill would need to be mocked or created properly
            
            technician.setSkills(skills);
            
            assertEquals(skills, technician.getSkills());
        }

        @Test
        @DisplayName("Should set and get assigned tickets correctly")
        void shouldSetAndGetAssignedTicketsCorrectly() {
            List<Ticket> tickets = new ArrayList<>();
            
            technician.setAssignedTickets(tickets);
            
            assertEquals(tickets, technician.getAssignedTickets());
        }

        @Test
        @DisplayName("Should set and get appointments correctly")
        void shouldSetAndGetAppointmentsCorrectly() {
            List<Appointment> appointments = new ArrayList<>();
            
            technician.setAppointments(appointments);
            
            assertEquals(appointments, technician.getAppointments());
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should have ACTIVE as default status")
        void shouldHaveActiveAsDefaultStatus() {
            Technician newTechnician = new Technician();
            
            assertEquals(TechnicianStatus.ACTIVE, newTechnician.getStatus());
        }

        @Test
        @DisplayName("Should set all status values correctly")
        void shouldSetAllStatusValuesCorrectly() {
            assertAll(
                () -> {
                    technician.setStatus(TechnicianStatus.ACTIVE);
                    assertEquals(TechnicianStatus.ACTIVE, technician.getStatus());
                },
                () -> {
                    technician.setStatus(TechnicianStatus.INACTIVE);
                    assertEquals(TechnicianStatus.INACTIVE, technician.getStatus());
                },
                () -> {
                    technician.setStatus(TechnicianStatus.IN_TRAINING);
                    assertEquals(TechnicianStatus.IN_TRAINING, technician.getStatus());
                },
                () -> {
                    technician.setStatus(TechnicianStatus.ON_VACATION);
                    assertEquals(TechnicianStatus.ON_VACATION, technician.getStatus());
                },
                () -> {
                    technician.setStatus(TechnicianStatus.TERMINATED);
                    assertEquals(TechnicianStatus.TERMINATED, technician.getStatus());
                }
            );
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should return correct current load when no open tickets")
        void shouldReturnCorrectCurrentLoadWhenNoOpenTickets() {
            List<Ticket> tickets = new ArrayList<>();
            
            // Mock closed ticket
            Ticket closedTicket = mock(Ticket.class);
            when(closedTicket.getStatus()).thenReturn(TicketStatus.CLOSED);
            tickets.add(closedTicket);
            
            technician.setAssignedTickets(tickets);
            
            assertEquals(0, technician.getCurrentLoad());
        }

        @Test
        @DisplayName("Should return correct current load with open tickets")
        void shouldReturnCorrectCurrentLoadWithOpenTickets() {
            List<Ticket> tickets = new ArrayList<>();
            
            // Mock open tickets
            Ticket openTicket1 = mock(Ticket.class);
            when(openTicket1.getStatus()).thenReturn(TicketStatus.OPEN);
            tickets.add(openTicket1);
            
            Ticket openTicket2 = mock(Ticket.class);
            when(openTicket2.getStatus()).thenReturn(TicketStatus.OPEN);
            tickets.add(openTicket2);
            
            // Mock closed ticket
            Ticket closedTicket = mock(Ticket.class);
            when(closedTicket.getStatus()).thenReturn(TicketStatus.CLOSED);
            tickets.add(closedTicket);
            
            technician.setAssignedTickets(tickets);
            
            assertEquals(2, technician.getCurrentLoad());
        }

        @Test
        @DisplayName("Should return zero current load when no tickets assigned")
        void shouldReturnZeroCurrentLoadWhenNoTicketsAssigned() {
            technician.setAssignedTickets(new ArrayList<>());
            
            assertEquals(0, technician.getCurrentLoad());
        }

        @Test
        @DisplayName("Should return true when qualified for service type")
        void shouldReturnTrueWhenQualifiedForServiceType() {
            Set<TechnicianSkill> skills = new HashSet<>();
            
            TechnicianSkill skill = mock(TechnicianSkill.class);
            when(skill.getServiceType()).thenReturn(ServiceType.HARDWARE);
            skills.add(skill);
            
            technician.setSkills(skills);
            
            assertTrue(technician.isQualifiedFor(ServiceType.HARDWARE));
        }

        @Test
        @DisplayName("Should return false when not qualified for service type")
        void shouldReturnFalseWhenNotQualifiedForServiceType() {
            Set<TechnicianSkill> skills = new HashSet<>();
            
            TechnicianSkill skill = mock(TechnicianSkill.class);
            when(skill.getServiceType()).thenReturn(ServiceType.HARDWARE);
            skills.add(skill);
            
            technician.setSkills(skills);
            
            assertFalse(technician.isQualifiedFor(ServiceType.SOFTWARE));
        }

        @Test
        @DisplayName("Should return false when no skills")
        void shouldReturnFalseWhenNoSkills() {
            technician.setSkills(new HashSet<>());
            
            assertFalse(technician.isQualifiedFor(ServiceType.HARDWARE));
        }

        @Test
        @DisplayName("Should handle multiple skills correctly")
        void shouldHandleMultipleSkillsCorrectly() {
            Set<TechnicianSkill> skills = new HashSet<>();
            
            TechnicianSkill skill1 = mock(TechnicianSkill.class);
            when(skill1.getServiceType()).thenReturn(ServiceType.HARDWARE);
            skills.add(skill1);
            
            TechnicianSkill skill2 = mock(TechnicianSkill.class);
            when(skill2.getServiceType()).thenReturn(ServiceType.SOFTWARE);
            skills.add(skill2);
            
            technician.setSkills(skills);
            
            assertAll(
                () -> assertTrue(technician.isQualifiedFor(ServiceType.HARDWARE)),
                () -> assertTrue(technician.isQualifiedFor(ServiceType.SOFTWARE))
            );
        }
    }

    @Nested
    @DisplayName("Collection Initialization Tests")
    class CollectionInitializationTests {

        @Test
        @DisplayName("Should initialize skills collection")
        void shouldInitializeSkillsCollection() {
            Technician newTechnician = new Technician();
            
            assertNotNull(newTechnician.getSkills());
            assertTrue(newTechnician.getSkills() instanceof HashSet);
        }

        @Test
        @DisplayName("Should initialize assigned tickets collection")
        void shouldInitializeAssignedTicketsCollection() {
            Technician newTechnician = new Technician();
            
            assertNotNull(newTechnician.getAssignedTickets());
            assertTrue(newTechnician.getAssignedTickets() instanceof ArrayList);
        }

        @Test
        @DisplayName("Should initialize appointments collection")
        void shouldInitializeAppointmentsCollection() {
            Technician newTechnician = new Technician();
            
            assertNotNull(newTechnician.getAppointments());
            assertTrue(newTechnician.getAppointments() instanceof ArrayList);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null skills collection gracefully")
        void shouldHandleNullSkillsCollectionGracefully() {
            technician.setSkills(null);
            
            assertThrows(NullPointerException.class, () -> {
                technician.isQualifiedFor(ServiceType.HARDWARE);
            });
        }

        @Test
        @DisplayName("Should handle null assigned tickets collection gracefully")
        void shouldHandleNullAssignedTicketsCollectionGracefully() {
            technician.setAssignedTickets(null);
            
            assertThrows(NullPointerException.class, () -> {
                technician.getCurrentLoad();
            });
        }

        @Test
        @DisplayName("Should handle empty email")
        void shouldHandleEmptyEmail() {
            technician.setEmail("");
            
            assertEquals("", technician.getEmail());
        }

        @Test
        @DisplayName("Should handle null full name")
        void shouldHandleNullFullName() {
            technician.setFullName(null);
            
            assertNull(technician.getFullName());
        }
    }

    @Test
    void testGetCurrentLoad() {
        // Create a technician
        Technician technician = new Technician();
        
        // Create mock tickets
        Ticket openTicket = mock(Ticket.class);
        Ticket closedTicket = mock(Ticket.class);
        
        // Define mock behavior
        when(openTicket.getStatus()).thenReturn(TicketStatus.OPEN);
        when(closedTicket.getStatus()).thenReturn(TicketStatus.CLOSED);
        
        // Set up the relationship
        technician.setAssignedTickets(Arrays.asList(openTicket, openTicket, closedTicket));
        
        // Test the method
        assertEquals(2, technician.getCurrentLoad());
    }
    
    @Test
    void testIsQualifiedFor() {
        Technician technician = new Technician();
        
        // Mock skills
        TechnicianSkill hardwareSkill = mock(TechnicianSkill.class);
        TechnicianSkill softwareSkill = mock(TechnicianSkill.class);
        
        when(hardwareSkill.getServiceType()).thenReturn(ServiceType.HARDWARE);
        when(softwareSkill.getServiceType()).thenReturn(ServiceType.SOFTWARE);
        
        technician.setSkills(Set.of(hardwareSkill, softwareSkill));
        
        assertTrue(technician.isQualifiedFor(ServiceType.HARDWARE));
        assertTrue(technician.isQualifiedFor(ServiceType.SOFTWARE));
    }
} 
