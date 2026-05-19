package com.crm.poc.lead.repository;

import com.crm.poc.data.InMemoryStore;
import com.crm.poc.lead.model.Lead;
import com.crm.poc.lead.model.LeadStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class LeadRepository {

    private final InMemoryStore<Lead> store;

    public LeadRepository(InMemoryStore<Lead> store) {
        this.store = store;
    }

    public List<Lead> findAll() {
        return store.findAll();
    }

    public Optional<Lead> findById(String id) {
        return store.findById(id);
    }

    public Lead save(Lead lead) {
        return store.save(lead);
    }

    public List<Lead> findByOwnerAndDateRange(String ownerId, LocalDateTime from, LocalDateTime to) {
        return store.findBy(lead ->
                lead.getOwnerId().equals(ownerId) &&
                !lead.getCreatedAt().isBefore(from) &&
                !lead.getCreatedAt().isAfter(to)
        ).stream()
                .sorted(Comparator.comparing(Lead::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Lead> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return store.findBy(lead ->
                !lead.getCreatedAt().isBefore(from) &&
                !lead.getCreatedAt().isAfter(to)
        ).stream()
                .sorted(Comparator.comparing(Lead::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Lead> findAllocatableByOwner(String ownerId) {
        return store.findBy(lead ->
                lead.getOwnerId().equals(ownerId) &&
                lead.getStatus().isAllocatable()
        );
    }

    public List<Lead> findByIds(List<String> ids) {
        return store.findBy(lead -> ids.contains(lead.getId()));
    }
}
