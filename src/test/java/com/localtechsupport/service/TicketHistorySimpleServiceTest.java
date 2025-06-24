package com.localtechsupport.service;

import com.localtechsupport.dto.request.CreateHistoryRequest;
import com.localtechsupport.entity.*;
import com.localtechsupport.repository.TicketHistoryRepository;
import com.localtechsupport.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for TicketHistoryService focusing on core functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketHistoryService Simplified Tests")
class TicketHistorySimpleServiceTest {

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketHistoryService ticketHistoryService;

    private Ticket testTicket;
    private TicketHistory testHistory;
    private Client testClient;

    @BeforeEach
    void setUp() {
        // Create test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setEmail("john.doe@example.com");
        testClient.setStatus(Client.ClientStatus.ACTIVE);

        // Create test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setDescription("Test ticket description");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setClient(testClient);
        testTicket.setServiceType(ServiceType.SOFTWARE);
        testTicket.setCreatedAt(Instant.now());

        // Create test history
        testHistory = new TicketHistory();
        testHistory.setId(1L);
        testHistory.setTicket(testTicket);
        testHistory.setStatus(TicketStatus.OPEN);
        testHistory.setDescription("Ticket created");
        testHistory.setCreatedBy("SYSTEM");
        testHistory.setCreatedAt(Instant.now());
        testHistory.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create history entry successfully")
    void shouldCreateHistoryEntrySuccessfully() {
        // Given
        CreateHistoryRequest request = new CreateHistoryRequest();
        request.setTicketId(1L);
        request.setStatus(TicketStatus.OPEN);
        request.setDescription("Test history entry");
        request.setCreatedBy("testuser");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketHistoryRepository.save(any(TicketHistory.class))).thenReturn(testHistory);

        // When
        TicketHistory result = ticketHistoryService.createHistoryEntry(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(ticketRepository).findById(1L);
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when ticket not found")
    void shouldThrowExceptionWhenTicketNotFound() {
        // Given
        CreateHistoryRequest request = new CreateHistoryRequest();
        request.setTicketId(999L);
        request.setStatus(TicketStatus.OPEN);
        request.setDescription("Test history entry");
        request.setCreatedBy("testuser");

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketHistoryService.createHistoryEntry(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ticket not found with ID: 999");
    }

    @Test
    @DisplayName("Should find history by ID")
    void shouldFindHistoryById() {
        // Given
        when(ticketHistoryRepository.findById(1L)).thenReturn(Optional.of(testHistory));

        // When
        Optional<TicketHistory> result = ticketHistoryService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testHistory);
    }

    @Test
    @DisplayName("Should get history by ID with exception for not found")
    void shouldGetHistoryByIdWithException() {
        // Given
        when(ticketHistoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketHistoryService.getHistoryById(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("History entry not found with ID: 999");
    }

    @Test
    @DisplayName("Should find all history with pagination")
    void shouldFindAllHistoryWithPagination() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<TicketHistory> expectedPage = new PageImpl<>(Collections.singletonList(testHistory));
        when(ticketHistoryRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<TicketHistory> result = ticketHistoryService.findAllHistory(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testHistory);
    }

    @Test
    @DisplayName("Should find history by ticket")
    void shouldFindHistoryByTicket() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<TicketHistory> expectedPage = new PageImpl<>(Collections.singletonList(testHistory));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketHistoryRepository.findByTicket(testTicket, pageable)).thenReturn(expectedPage);

        // When
        Page<TicketHistory> result = ticketHistoryService.findHistoryByTicket(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get ticket timeline")
    void shouldGetTicketTimeline() {
        // Given
        List<TicketHistory> timeline = Collections.singletonList(testHistory);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketHistoryRepository.findByTicketOrderByCreatedAtAsc(testTicket)).thenReturn(timeline);

        // When
        List<TicketHistory> result = ticketHistoryService.getTicketTimeline(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testHistory);
    }

    @Test
    @DisplayName("Should find history by status")
    void shouldFindHistoryByStatus() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<TicketHistory> expectedPage = new PageImpl<>(Collections.singletonList(testHistory));
        when(ticketHistoryRepository.findByStatus(TicketStatus.OPEN, pageable)).thenReturn(expectedPage);

        // When
        Page<TicketHistory> result = ticketHistoryService.findHistoryByStatus(TicketStatus.OPEN, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should count history for ticket")
    void shouldCountHistoryForTicket() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketHistoryRepository.countByTicket(testTicket)).thenReturn(5L);

        // When
        long result = ticketHistoryService.countHistoryForTicket(1L);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should count history by status")
    void shouldCountHistoryByStatus() {
        // Given
        when(ticketHistoryRepository.countByStatus(TicketStatus.OPEN)).thenReturn(10L);

        // When
        long result = ticketHistoryService.countHistoryByStatus(TicketStatus.OPEN);

        // Then
        assertThat(result).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should validate and sanitize description")
    void shouldValidateAndSanitizeDescription() {
        // Given
        CreateHistoryRequest request = new CreateHistoryRequest();
        request.setTicketId(1L);
        request.setStatus(TicketStatus.OPEN);
        request.setDescription("  Test description with extra spaces  ");
        request.setCreatedBy("testuser");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketHistoryRepository.save(any(TicketHistory.class))).thenReturn(testHistory);

        // When
        ticketHistoryService.createHistoryEntry(request);

        // Then
        verify(ticketHistoryRepository).save(argThat(history -> 
            history.getDescription().equals("Test description with extra spaces")));
    }

    @Test
    @DisplayName("Should throw exception for empty description")
    void shouldThrowExceptionForEmptyDescription() {
        // Given
        CreateHistoryRequest request = new CreateHistoryRequest();
        request.setTicketId(1L);
        request.setStatus(TicketStatus.OPEN);
        request.setDescription("");
        request.setCreatedBy("testuser");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When & Then
        assertThatThrownBy(() -> ticketHistoryService.createHistoryEntry(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Description cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void shouldHandleNullParametersGracefully() {
        // When & Then
        PageRequest pageable = PageRequest.of(0, 10);
        
        assertThatThrownBy(() -> ticketHistoryService.findHistoryByDateRange(null, Instant.now(), pageable))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ticketHistoryService.searchHistoryByDescription(null, pageable))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ticketHistoryService.findHistoryByCreatedBy(null, pageable))
            .isInstanceOf(IllegalArgumentException.class);
    }
}