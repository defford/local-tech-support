package com.localtechsupport.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Entity Tests")
class ClientTest {

    private Validator validator;
    private Client client;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid client for testing
        client = new Client("John", "Doe", "john.doe@example.com");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create client with default constructor")
        void shouldCreateClientWithDefaultConstructor() {
            Client newClient = new Client();
            
            assertNotNull(newClient);
            assertNull(newClient.getId());
            assertEquals(Client.ClientStatus.ACTIVE, newClient.getStatus());
        }

        @Test
        @DisplayName("Should create client with parameterized constructor")
        void shouldCreateClientWithParameterizedConstructor() {
            Client newClient = new Client("Jane", "Smith", "jane.smith@example.com");
            
            assertNotNull(newClient);
            assertEquals("Jane", newClient.getFirstName());
            assertEquals("Smith", newClient.getLastName());
            assertEquals("jane.smith@example.com", newClient.getEmail());
            assertEquals(Client.ClientStatus.ACTIVE, newClient.getStatus());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid client")
        void shouldPassValidationWithValidClient() {
            Set<ConstraintViolation<Client>> violations = validator.validate(client);
            
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when first name is blank")
        void shouldFailValidationWhenFirstNameIsBlank() {
            client.setFirstName("");
            
            Set<ConstraintViolation<Client>> violations = validator.validate(client);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("First name is required")));
        }

        @Test
        @DisplayName("Should fail validation when last name is blank")
        void shouldFailValidationWhenLastNameIsBlank() {
            client.setLastName("");
            
            Set<ConstraintViolation<Client>> violations = validator.validate(client);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Last name is required")));
        }

        @Test
        @DisplayName("Should fail validation when email is invalid")
        void shouldFailValidationWhenEmailIsInvalid() {
            client.setEmail("invalid-email");
            
            Set<ConstraintViolation<Client>> violations = validator.validate(client);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
        }

        @Test
        @DisplayName("Should fail validation when email is blank")
        void shouldFailValidationWhenEmailIsBlank() {
            client.setEmail("");
            
            Set<ConstraintViolation<Client>> violations = validator.validate(client);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return full name correctly")
        void shouldReturnFullNameCorrectly() {
            String fullName = client.getFullName();
            
            assertEquals("John Doe", fullName);
        }   

       
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should set and get client status correctly")
        void shouldSetAndGetClientStatusCorrectly() {
            client.setStatus(Client.ClientStatus.SUSPENDED);
            
            assertEquals(Client.ClientStatus.SUSPENDED, client.getStatus());
        }

        

        @Test
        @DisplayName("Should have correct default values for enums")
        void shouldHaveCorrectDefaultValuesForEnums() {
            Client newClient = new Client();
            
            assertEquals(Client.ClientStatus.ACTIVE, newClient.getStatus());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should not be equal when id is different")
        void shouldNotBeEqualWhenIdIsDifferent() {
            Client client1 = new Client("John", "Doe", "john@example.com");
            client1.setId(1L);
            
            Client client2 = new Client("John", "Doe", "john@example.com");
            client2.setId(2L);
            
            assertNotEquals(client1, client2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(client, null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals(client, "not a client");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(client, client);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain key information in toString")
        void shouldContainKeyInformationInToString() {
            client.setId(1L);
            client.setStatus(Client.ClientStatus.ACTIVE);
            
            String toString = client.toString();
            
            assertAll(
                () -> assertTrue(toString.contains("id=1")),
                () -> assertTrue(toString.contains("firstName='John'")),
                () -> assertTrue(toString.contains("lastName='Doe'")),
                () -> assertTrue(toString.contains("email='john.doe@example.com'")),
                () -> assertTrue(toString.contains("status=ACTIVE"))
            );
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get timestamps correctly")
        void shouldSetAndGetTimestampsCorrectly() {
            LocalDateTime now = LocalDateTime.now();
            client.setCreatedAt(now);
            client.setUpdatedAt(now);
            
            assertEquals(now, client.getCreatedAt());
            assertEquals(now, client.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get all properties correctly")
        void shouldSetAndGetAllPropertiesCorrectly() {
            client.setId(1L);
            client.setPhone("555-0123");
            client.setAddress("456 Oak Ave");
            client.setNotes("VIP client");
            
            assertAll(
                () -> assertEquals(1L, client.getId()),
                () -> assertEquals("555-0123", client.getPhone()),
                () -> assertEquals("456 Oak Ave", client.getAddress()),
                () -> assertEquals("VIP client", client.getNotes())
            );
        }
    }
}
