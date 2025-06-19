package com.localtechsupport.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Appointment Entity Tests")
class AppointmentTest {

    @Mock
    private Technician mockTechnician;
    
    @Mock
    private Ticket mockTicket;

    private Appointment appointment;
    private Instant startTime;
    private Instant endTime;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        startTime = Instant.now().plus(1, ChronoUnit.HOURS);
        endTime = startTime.plus(2, ChronoUnit.HOURS);
        
        // Set up basic appointment properties
        appointment.setTechnician(mockTechnician);
        appointment.setTicket(mockTicket);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create appointment with default constructor")
        void shouldCreateAppointmentWithDefaultConstructor() {
            Appointment newAppointment = new Appointment();
            
            assertNotNull(newAppointment);
            assertNull(newAppointment.getId());
            assertEquals(AppointmentStatus.PENDING, newAppointment.getStatus());
            assertNull(newAppointment.getTechnician());
            assertNull(newAppointment.getTicket());
            assertNull(newAppointment.getStartTime());
            assertNull(newAppointment.getEndTime());
            assertNull(newAppointment.getCreatedAt());
            assertNull(newAppointment.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            appointment.setId(1L);
            
            assertEquals(1L, appointment.getId());
        }

        @Test
        @DisplayName("Should set and get technician correctly")
        void shouldSetAndGetTechnicianCorrectly() {
            Technician newTechnician = mock(Technician.class);
            appointment.setTechnician(newTechnician);
            
            assertEquals(newTechnician, appointment.getTechnician());
        }

        @Test
        @DisplayName("Should set and get ticket correctly")
        void shouldSetAndGetTicketCorrectly() {
            Ticket newTicket = mock(Ticket.class);
            appointment.setTicket(newTicket);
            
            assertEquals(newTicket, appointment.getTicket());
        }

        @Test
        @DisplayName("Should set and get start time correctly")
        void shouldSetAndGetStartTimeCorrectly() {
            Instant newStartTime = Instant.now().plus(3, ChronoUnit.HOURS);
            appointment.setStartTime(newStartTime);
            
            assertEquals(newStartTime, appointment.getStartTime());
        }

        @Test
        @DisplayName("Should set and get end time correctly")
        void shouldSetAndGetEndTimeCorrectly() {
            Instant newEndTime = Instant.now().plus(4, ChronoUnit.HOURS);
            appointment.setEndTime(newEndTime);
            
            assertEquals(newEndTime, appointment.getEndTime());
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatusCorrectly() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            
            assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        }

        @Test
        @DisplayName("Should set and get created at correctly")
        void shouldSetAndGetCreatedAtCorrectly() {
            Instant createdAt = Instant.now();
            appointment.setCreatedAt(createdAt);
            
            assertEquals(createdAt, appointment.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updated at correctly")
        void shouldSetAndGetUpdatedAtCorrectly() {
            Instant updatedAt = Instant.now();
            appointment.setUpdatedAt(updatedAt);
            
            assertEquals(updatedAt, appointment.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should have PENDING as default status")
        void shouldHavePendingAsDefaultStatus() {
            Appointment newAppointment = new Appointment();
            
            assertEquals(AppointmentStatus.PENDING, newAppointment.getStatus());
        }

        @Test
        @DisplayName("Should set all status values correctly")
        void shouldSetAllStatusValuesCorrectly() {
            assertAll(
                () -> {
                    appointment.setStatus(AppointmentStatus.PENDING);
                    assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
                },
                () -> {
                    appointment.setStatus(AppointmentStatus.CONFIRMED);
                    assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
                },
                () -> {
                    appointment.setStatus(AppointmentStatus.IN_PROGRESS);
                    assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
                },
                () -> {
                    appointment.setStatus(AppointmentStatus.COMPLETED);
                    assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
                },
                () -> {
                    appointment.setStatus(AppointmentStatus.CANCELLED);
                    assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
                },
                () -> {
                    appointment.setStatus(AppointmentStatus.NO_SHOW);
                    assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
                }
            );
        }
    }

    @Nested
    @DisplayName("Time Validation Tests")
    class TimeValidationTests {

        @Test
        @DisplayName("Should handle valid time range")
        void shouldHandleValidTimeRange() {
            Instant validStart = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant validEnd = validStart.plus(2, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(validStart);
                appointment.setEndTime(validEnd);
                
                assertEquals(validStart, appointment.getStartTime());
                assertEquals(validEnd, appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle same start and end time")
        void shouldHandleSameStartAndEndTime() {
            Instant sameTime = Instant.now().plus(1, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(sameTime);
                appointment.setEndTime(sameTime);
                
                assertEquals(sameTime, appointment.getStartTime());
                assertEquals(sameTime, appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle end time before start time")
        void shouldHandleEndTimeBeforeStartTime() {
            Instant laterTime = Instant.now().plus(2, ChronoUnit.HOURS);
            Instant earlierTime = Instant.now().plus(1, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(laterTime);
                appointment.setEndTime(earlierTime);
                
                assertEquals(laterTime, appointment.getStartTime());
                assertEquals(earlierTime, appointment.getEndTime());
            });
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain technician relationship")
        void shouldMaintainTechnicianRelationship() {
            assertNotNull(appointment.getTechnician());
            assertEquals(mockTechnician, appointment.getTechnician());
        }

        @Test
        @DisplayName("Should maintain ticket relationship")
        void shouldMaintainTicketRelationship() {
            assertNotNull(appointment.getTicket());
            assertEquals(mockTicket, appointment.getTicket());
        }

        @Test
        @DisplayName("Should handle null technician")
        void shouldHandleNullTechnician() {
            assertDoesNotThrow(() -> {
                appointment.setTechnician(null);
                assertNull(appointment.getTechnician());
            });
        }

        @Test
        @DisplayName("Should handle null ticket")
        void shouldHandleNullTicket() {
            assertDoesNotThrow(() -> {
                appointment.setTicket(null);
                assertNull(appointment.getTicket());
            });
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {

        @Test
        @DisplayName("Should set created at timestamp on persist")
        void shouldSetCreatedAtTimestampOnPersist() {
            Appointment newAppointment = new Appointment();
            Instant beforePersist = Instant.now();
            
            // Simulate @PrePersist
            newAppointment.onCreate();
            
            assertNotNull(newAppointment.getCreatedAt());
            assertTrue(newAppointment.getCreatedAt().isAfter(beforePersist.minus(1, ChronoUnit.SECONDS)));
            assertTrue(newAppointment.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should set updated at timestamp on update")
        void shouldSetUpdatedAtTimestampOnUpdate() {
            appointment.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS)); // Set earlier created time
            Instant beforeUpdate = Instant.now();
            
            // Simulate @PreUpdate
            appointment.onUpdate();
            
            assertNotNull(appointment.getUpdatedAt());
            assertTrue(appointment.getUpdatedAt().isAfter(beforeUpdate.minus(1, ChronoUnit.SECONDS)));
            assertTrue(appointment.getUpdatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should not affect created at timestamp on update")
        void shouldNotAffectCreatedAtTimestampOnUpdate() {
            Instant originalCreatedAt = Instant.now().minus(1, ChronoUnit.HOURS);
            appointment.setCreatedAt(originalCreatedAt);
            
            // Simulate @PreUpdate
            appointment.onUpdate();
            
            assertEquals(originalCreatedAt, appointment.getCreatedAt());
            assertNotNull(appointment.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null start time")
        void shouldHandleNullStartTime() {
            assertDoesNotThrow(() -> {
                appointment.setStartTime(null);
                assertNull(appointment.getStartTime());
            });
        }

        @Test
        @DisplayName("Should handle null end time")
        void shouldHandleNullEndTime() {
            assertDoesNotThrow(() -> {
                appointment.setEndTime(null);
                assertNull(appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle past appointment times")
        void shouldHandlePastAppointmentTimes() {
            Instant pastStart = Instant.now().minus(2, ChronoUnit.HOURS);
            Instant pastEnd = Instant.now().minus(1, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(pastStart);
                appointment.setEndTime(pastEnd);
                
                assertEquals(pastStart, appointment.getStartTime());
                assertEquals(pastEnd, appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle future appointment times")
        void shouldHandleFutureAppointmentTimes() {
            Instant futureStart = Instant.now().plus(24, ChronoUnit.HOURS);
            Instant futureEnd = futureStart.plus(2, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(futureStart);
                appointment.setEndTime(futureEnd);
                
                assertEquals(futureStart, appointment.getStartTime());
                assertEquals(futureEnd, appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle appointment with very short duration")
        void shouldHandleAppointmentWithVeryShortDuration() {
            Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant end = start.plus(1, ChronoUnit.MINUTES);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(start);
                appointment.setEndTime(end);
                
                assertEquals(start, appointment.getStartTime());
                assertEquals(end, appointment.getEndTime());
            });
        }

        @Test
        @DisplayName("Should handle appointment with very long duration")
        void shouldHandleAppointmentWithVeryLongDuration() {
            Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant end = start.plus(8, ChronoUnit.HOURS);
            
            assertDoesNotThrow(() -> {
                appointment.setStartTime(start);
                appointment.setEndTime(end);
                
                assertEquals(start, appointment.getStartTime());
                assertEquals(end, appointment.getEndTime());
            });
        }
    }
} 