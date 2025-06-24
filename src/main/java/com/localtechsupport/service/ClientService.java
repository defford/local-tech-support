package com.localtechsupport.service;

import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Client.ClientStatus;
import com.localtechsupport.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Client management operations.
 * 
 * Provides business logic for:
 * - Client CRUD operations with validation
 * - Status management and lifecycle tracking
 * - Search and filtering capabilities
 * - Statistics and reporting
 * - Email uniqueness validation
 */
@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Creates a new client with validation and email uniqueness check.
     */
    public Client createClient(String firstName, String lastName, String email, String phone, 
                              String address, String notes) {
        // Validate email uniqueness
        if (clientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Client with email " + email + " already exists");
        }

        // Validate required fields
        validateRequiredFields(firstName, lastName, email);

        // Create client
        Client client = new Client();
        client.setFirstName(firstName.trim());
        client.setLastName(lastName.trim());
        client.setEmail(email.trim().toLowerCase());
        client.setPhone(phone != null ? phone.trim() : null);
        client.setAddress(address != null ? address.trim() : null);
        client.setNotes(notes != null ? notes.trim() : null);
        client.setStatus(ClientStatus.ACTIVE);

        return clientRepository.save(client);
    }

    /**
     * Updates an existing client with validation.
     */
    public Client updateClient(Long clientId, String firstName, String lastName, String email, 
                              String phone, String address, String notes) {
        Client client = getClientById(clientId);

        // Check email uniqueness if email is being changed
        if (email != null && !email.equalsIgnoreCase(client.getEmail())) {
            if (clientRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Client with email " + email + " already exists");
            }
            client.setEmail(email.trim().toLowerCase());
        }

        // Update fields if provided
        if (firstName != null && !firstName.trim().isEmpty()) {
            client.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            client.setLastName(lastName.trim());
        }
        if (phone != null) {
            client.setPhone(phone.trim().isEmpty() ? null : phone.trim());
        }
        if (address != null) {
            client.setAddress(address.trim().isEmpty() ? null : address.trim());
        }
        if (notes != null) {
            client.setNotes(notes.trim().isEmpty() ? null : notes.trim());
        }

        return clientRepository.save(client);
    }

    /**
     * Deletes a client by ID.
     */
    public void deleteClient(Long clientId) {
        Client client = getClientById(clientId);
        
        // Business rule: Only allow deletion of inactive clients
        if (client.getStatus() == ClientStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active client. Please deactivate first.");
        }

        clientRepository.deleteById(clientId);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Updates client status with validation.
     */
    public Client updateClientStatus(Long clientId, ClientStatus newStatus, String reason) {
        Client client = getClientById(clientId);

        if (!isValidStatusTransition(client.getStatus(), newStatus)) {
            throw new IllegalStateException(
                "Invalid status transition from " + client.getStatus() + " to " + newStatus);
        }

        client.setStatus(newStatus);
        return clientRepository.save(client);
    }

    /**
     * Deactivates a client.
     */
    public Client deactivateClient(Long clientId, String reason) {
        return updateClientStatus(clientId, ClientStatus.INACTIVE, reason);
    }

    /**
     * Reactivates a client.
     */
    public Client activateClient(Long clientId, String reason) {
        return updateClientStatus(clientId, ClientStatus.ACTIVE, reason);
    }

    /**
     * Suspends a client.
     */
    public Client suspendClient(Long clientId, String reason) {
        return updateClientStatus(clientId, ClientStatus.SUSPENDED, reason);
    }

    // === SEARCH AND RETRIEVAL METHODS ===

    @Transactional(readOnly = true)
    public Optional<Client> findById(Long clientId) {
        return clientRepository.findById(clientId);
    }

    @Transactional(readOnly = true)
    public Client getClientById(Long clientId) {
        return clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + clientId));
    }

    @Transactional(readOnly = true)
    public Optional<Client> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return clientRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Client getClientByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public Page<Client> findAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findClientsByStatus(ClientStatus status, Pageable pageable) {
        return clientRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> searchClients(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllClients(pageable);
        }
        return clientRepository.searchClients(searchTerm.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findRecentClients(LocalDateTime since, Pageable pageable) {
        return clientRepository.findByCreatedAtAfter(since, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findClientsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return clientRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findActiveClients(Pageable pageable) {
        return findClientsByStatus(ClientStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findInactiveClients(Pageable pageable) {
        return findClientsByStatus(ClientStatus.INACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findSuspendedClients(Pageable pageable) {
        return findClientsByStatus(ClientStatus.SUSPENDED, pageable);
    }

    // === STATISTICS AND REPORTING ===

    @Transactional(readOnly = true)
    public long countAllClients() {
        return clientRepository.count();
    }

    @Transactional(readOnly = true)
    public long countClientsByStatus(ClientStatus status) {
        return clientRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countActiveClients() {
        return countClientsByStatus(ClientStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countInactiveClients() {
        return countClientsByStatus(ClientStatus.INACTIVE);
    }

    @Transactional(readOnly = true)
    public long countSuspendedClients() {
        return countClientsByStatus(ClientStatus.SUSPENDED);
    }

    @Transactional(readOnly = true)
    public long countRecentClients(LocalDateTime since) {
        return clientRepository.countByCreatedAtAfter(since);
    }

    @Transactional(readOnly = true)
    public Map<ClientStatus, Long> getClientCountsByStatus() {
        Map<ClientStatus, Long> counts = new HashMap<>();
        counts.put(ClientStatus.ACTIVE, countActiveClients());
        counts.put(ClientStatus.INACTIVE, countInactiveClients());
        counts.put(ClientStatus.SUSPENDED, countSuspendedClients());
        return counts;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClientStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalClients", countAllClients());
        stats.put("activeClients", countActiveClients());
        stats.put("inactiveClients", countInactiveClients());
        stats.put("suspendedClients", countSuspendedClients());
        
        // Recent statistics (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("recentClients", countRecentClients(thirtyDaysAgo));
        
        // Percentage calculations
        long total = countAllClients();
        if (total > 0) {
            stats.put("activePercentage", Math.round((countActiveClients() * 100.0) / total));
            stats.put("inactivePercentage", Math.round((countInactiveClients() * 100.0) / total));
            stats.put("suspendedPercentage", Math.round((countSuspendedClients() * 100.0) / total));
        }
        
        return stats;
    }

    // === EMAIL VALIDATION UTILITIES ===

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !clientRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return !isEmailAvailable(email);
    }

    // === PRIVATE HELPER METHODS ===

    private void validateRequiredFields(String firstName, String lastName, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation - more comprehensive validation is handled by entity annotations
        return email != null && email.trim().contains("@") && email.trim().contains(".");
    }

    private boolean isValidStatusTransition(ClientStatus from, ClientStatus to) {
        if (from == to) {
            return true; // Same status is always valid
        }

        switch (from) {
            case ACTIVE:
                return to == ClientStatus.INACTIVE || to == ClientStatus.SUSPENDED;
            case INACTIVE:
                return to == ClientStatus.ACTIVE || to == ClientStatus.SUSPENDED;
            case SUSPENDED:
                return to == ClientStatus.ACTIVE || to == ClientStatus.INACTIVE;
            default:
                return false;
        }
    }
} 