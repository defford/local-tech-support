package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.Client;
import com.localtechsupport.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Client management operations.
 * 
 * Provides comprehensive API endpoints for:
 * - Client CRUD operations
 * - Status management
 * - Search and filtering
 * - Statistics and reporting
 */
@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Create a new client.
     */
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.createClient(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getNotes()
        );
        
        ClientResponse response = ClientResponse.from(client);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a client by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        
        if (client.isPresent()) {
            ClientResponse response = ClientResponse.from(client.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update an existing client.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {
        
        Client client = clientService.updateClient(
            id,
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getNotes()
        );
        
        ClientResponse response = ClientResponse.from(client);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a client by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all clients with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Client.ClientStatus status) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients;
        
        // Apply filters based on query parameters
        if (status != null) {
            clients = clientService.findClientsByStatus(status, pageable);
        } else {
            clients = clientService.findAllClients(pageable);
        }
        
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        return ResponseEntity.ok(response);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Update client status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ClientResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientStatusRequest request) {
        
        Client client = clientService.updateClientStatus(
            id, 
            request.getStatus(), 
            request.getReason()
        );
        
        ClientResponse response = ClientResponse.from(client);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a client.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ClientResponse> activateClient(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        Client client = clientService.activateClient(id, reason);
        ClientResponse response = ClientResponse.from(client);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a client.
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ClientResponse> deactivateClient(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        Client client = clientService.deactivateClient(id, reason);
        ClientResponse response = ClientResponse.from(client);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend a client.
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ClientResponse> suspendClient(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        Client client = clientService.suspendClient(id, reason);
        ClientResponse response = ClientResponse.from(client);
        return ResponseEntity.ok(response);
    }

    // === SEARCH AND FILTERING ===

    /**
     * Search clients by name or email.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients = clientService.searchClients(query, pageable);
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get clients by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ClientResponse>> getClientsByStatus(
            @PathVariable Client.ClientStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients = clientService.findClientsByStatus(status, pageable);
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active clients.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ClientResponse>> getActiveClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients = clientService.findActiveClients(pageable);
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get inactive clients.
     */
    @GetMapping("/inactive")
    public ResponseEntity<Page<ClientResponse>> getInactiveClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients = clientService.findInactiveClients(pageable);
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent clients.
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<ClientResponse>> getRecentClients(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Page<Client> clients = clientService.findRecentClients(since, pageable);
        Page<ClientResponse> response = clients.map(ClientResponse::from);
        
        return ResponseEntity.ok(response);
    }

    // === STATISTICS AND REPORTING ===

    /**
     * Get comprehensive client statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ClientStatisticsResponse> getStatistics() {
        // Gather statistics from service layer
        long totalClients = clientService.countAllClients();
        long activeClients = clientService.countActiveClients();
        long inactiveClients = clientService.countInactiveClients();
        long suspendedClients = clientService.countSuspendedClients();
        
        // Get time-based statistics
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStart = today.minusDays(7);
        LocalDateTime monthStart = today.minusDays(30);
        
        long clientsCreatedToday = clientService.countRecentClients(today);
        long clientsCreatedThisWeek = clientService.countRecentClients(weekStart);
        long clientsCreatedThisMonth = clientService.countRecentClients(monthStart);
        
        // Create response with comprehensive statistics
        ClientStatisticsResponse response = new ClientStatisticsResponse(
            totalClients, activeClients, inactiveClients,
            clientsCreatedToday, clientsCreatedThisWeek, clientsCreatedThisMonth,
            null, // clientsByStatus map will be set below
            monthStart, // dataFromDate
            LocalDateTime.now() // dataToDate
        );
        
        // Add status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("ACTIVE", activeClients);
        statusBreakdown.put("INACTIVE", inactiveClients);
        statusBreakdown.put("SUSPENDED", suspendedClients);
        response.setClientsByStatus(statusBreakdown);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get total client count.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getClientCount() {
        Map<String, Long> count = new HashMap<>();
        count.put("total", clientService.countAllClients());
        return ResponseEntity.ok(count);
    }

    /**
     * Get client count by status.
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Map<String, Long>> getClientCountByStatus(
            @PathVariable Client.ClientStatus status) {
        
        Map<String, Long> count = new HashMap<>();
        count.put(status.toString().toLowerCase(), clientService.countClientsByStatus(status));
        return ResponseEntity.ok(count);
    }

    /**
     * Get client counts by all statuses.
     */
    @GetMapping("/count/status")
    public ResponseEntity<Map<Client.ClientStatus, Long>> getClientCountsByStatus() {
        Map<Client.ClientStatus, Long> counts = clientService.getClientCountsByStatus();
        return ResponseEntity.ok(counts);
    }

    /**
     * Check if email is available.
     */
    @GetMapping("/email/available")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @RequestParam String email) {
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", clientService.isEmailAvailable(email));
        return ResponseEntity.ok(result);
    }
} 