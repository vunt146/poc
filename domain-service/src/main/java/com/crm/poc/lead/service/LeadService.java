package com.crm.poc.lead.service;

import com.crm.poc.lead.model.*;
import com.crm.poc.lead.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<Lead> findLeads(String ownerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        if (ownerId != null && !ownerId.isBlank()) {
            return leadRepository.findByOwnerAndDateRange(ownerId, thirtyDaysAgo, now);
        }
        return leadRepository.findByDateRange(thirtyDaysAgo, now);
    }

    public Lead findById(String id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead không tồn tại: " + id));
    }

    public Lead updateStatus(String leadId, LeadStatus newStatus, String changedBy, String note, String reason) {
        Lead lead = findById(leadId);

        if (!LeadStateTransitions.canTransition(lead.getStatus(), newStatus, lead.getHistory())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển từ %s sang %s", lead.getStatus(), newStatus));
        }

        // Special validation: NEW_LEAD -> CONTACTED via CALL requires note
        if ((lead.getStatus() == LeadStatus.NEW_LEAD || lead.getStatus() == LeadStatus.NEW_IMPORTED_LEAD)
                && newStatus == LeadStatus.CONTACTED
                && (note == null || note.isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập kết quả cuộc gọi");
        }

        LeadHistoryEntry historyEntry = new LeadHistoryEntry(
                UUID.randomUUID().toString(),
                leadId,
                lead.getStatus(),
                newStatus,
                changedBy,
                LocalDateTime.now(),
                note,
                reason
        );

        lead.setStatus(newStatus);
        lead.setUpdatedAt(LocalDateTime.now());
        lead.getHistory().add(historyEntry);

        log.info("Lead {} status updated: {} -> {}", leadId, historyEntry.getPreviousStatus(), newStatus);
        return leadRepository.save(lead);
    }

    public List<Lead> findAllocatableLeads(String ownerId) {
        return leadRepository.findAllocatableByOwner(ownerId);
    }

    public List<Lead> findByIds(List<String> ids) {
        return leadRepository.findByIds(ids);
    }
}
