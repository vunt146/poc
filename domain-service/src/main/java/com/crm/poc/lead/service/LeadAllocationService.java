package com.crm.poc.lead.service;

import com.crm.poc.lead.model.Lead;
import com.crm.poc.lead.model.LeadHistoryEntry;
import com.crm.poc.lead.model.LeadStatus;
import com.crm.poc.lead.repository.LeadRepository;
import com.crm.poc.user.model.User;
import com.crm.poc.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeadAllocationService {

    private static final Logger log = LoggerFactory.getLogger(LeadAllocationService.class);
    private static final int MAX_USERS_PER_ALLOCATION = 10;

    private final LeadRepository leadRepository;
    private final UserService userService;

    public LeadAllocationService(LeadRepository leadRepository, UserService userService) {
        this.leadRepository = leadRepository;
        this.userService = userService;
    }

    public AllocationResult allocate(List<String> leadIds, List<String> targetUserIds, String requestedBy) {
        // Validation
        validate(leadIds, targetUserIds);

        // Get and sort users by username (ascending, case-insensitive)
        List<User> sortedUsers = targetUserIds.stream()
                .map(userService::findById)
                .sorted(Comparator.comparing(u -> u.getUsername().toLowerCase()))
                .collect(Collectors.toList());

        // Shuffle leads randomly
        List<String> shuffledLeadIds = new ArrayList<>(leadIds);
        Collections.shuffle(shuffledLeadIds);

        // Calculate distribution
        Map<String, List<String>> allocations = calculateDistribution(shuffledLeadIds, sortedUsers);

        // Execute allocation - update lead owners
        for (Map.Entry<String, List<String>> entry : allocations.entrySet()) {
            String userId = entry.getKey();
            for (String leadId : entry.getValue()) {
                Lead lead = leadRepository.findById(leadId)
                        .orElseThrow(() -> new IllegalArgumentException("Lead không tồn tại: " + leadId));

                lead.setOwnerId(userId);
                lead.setUpdatedAt(LocalDateTime.now());

                LeadHistoryEntry historyEntry = new LeadHistoryEntry(
                        UUID.randomUUID().toString(),
                        leadId,
                        lead.getStatus(),
                        lead.getStatus(), // status doesn't change during allocation
                        requestedBy,
                        LocalDateTime.now(),
                        "Phân bổ cho " + userId,
                        "Phân bổ bởi quản lý"
                );
                lead.getHistory().add(historyEntry);
                leadRepository.save(lead);
            }
        }

        log.info("Allocated {} leads to {} users by {}", leadIds.size(), targetUserIds.size(), requestedBy);

        return new AllocationResult(true, allocations, leadIds.size(),
                String.format("Phân bổ thành công %d cơ hội cho %d cán bộ", leadIds.size(), targetUserIds.size()));
    }

    /**
     * Calculate fair distribution of leads among users.
     * Users sorted by username alphabetically receive remainder leads first.
     */
    public Map<String, List<String>> calculateDistribution(List<String> leadIds, List<User> sortedUsers) {
        int totalLeads = leadIds.size();
        int totalUsers = sortedUsers.size();
        int quotient = totalLeads / totalUsers;
        int remainder = totalLeads % totalUsers;

        Map<String, List<String>> allocations = new LinkedHashMap<>();
        int leadIndex = 0;

        for (int i = 0; i < totalUsers; i++) {
            int count = quotient + (i < remainder ? 1 : 0);
            List<String> userLeads = new ArrayList<>(leadIds.subList(leadIndex, leadIndex + count));
            allocations.put(sortedUsers.get(i).getId(), userLeads);
            leadIndex += count;
        }

        return allocations;
    }

    private void validate(List<String> leadIds, List<String> targetUserIds) {
        if (leadIds == null || leadIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 Lead");
        }
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 cán bộ");
        }
        if (targetUserIds.size() > MAX_USERS_PER_ALLOCATION) {
            throw new IllegalArgumentException("Tối đa " + MAX_USERS_PER_ALLOCATION + " cán bộ phụ trách");
        }

        // Validate all leads exist and are in allocatable status
        for (String leadId : leadIds) {
            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new IllegalArgumentException("Lead không tồn tại: " + leadId));
            if (!lead.getStatus().isAllocatable()) {
                throw new IllegalArgumentException(
                        "Lead " + leadId + " không ở trạng thái cho phép phân bổ (hiện tại: " + lead.getStatus() + ")");
            }
        }
    }

    public record AllocationResult(
            boolean success,
            Map<String, List<String>> allocations,
            int totalLeadsAllocated,
            String message
    ) {}
}
