package com.crm.poc.lead.model;

import java.util.*;

/**
 * Defines allowed state transitions for Lead lifecycle.
 */
public final class LeadStateTransitions {

    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(LeadStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(LeadStatus.NEW_LEAD,
                EnumSet.of(LeadStatus.CONTACTED, LeadStatus.PROCESSING, LeadStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(LeadStatus.NEW_IMPORTED_LEAD,
                EnumSet.of(LeadStatus.CONTACTED, LeadStatus.PROCESSING, LeadStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(LeadStatus.CONTACTED,
                EnumSet.of(LeadStatus.PROCESSING, LeadStatus.DOCUMENT_COLLECTED, LeadStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(LeadStatus.PROCESSING,
                EnumSet.of(LeadStatus.DOCUMENT_COLLECTED, LeadStatus.COMPLETED, LeadStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(LeadStatus.DOCUMENT_COLLECTED,
                EnumSet.of(LeadStatus.COMPLETED, LeadStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(LeadStatus.COMPLETED, EnumSet.noneOf(LeadStatus.class));
        ALLOWED_TRANSITIONS.put(LeadStatus.REJECTED, EnumSet.noneOf(LeadStatus.class));
    }

    private LeadStateTransitions() {}

    public static boolean canTransition(LeadStatus current, LeadStatus target, List<LeadHistoryEntry> history) {
        if (current.isTerminal()) {
            return false;
        }

        Set<LeadStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(LeadStatus.class));
        if (!allowed.contains(target)) {
            return false;
        }

        // Cannot go back to a status already visited
        Set<LeadStatus> visited = new HashSet<>();
        for (LeadHistoryEntry entry : history) {
            visited.add(entry.getNewStatus());
        }
        return !visited.contains(target);
    }

    public static Set<LeadStatus> getAllowedTransitions(LeadStatus current) {
        return Collections.unmodifiableSet(
                ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(LeadStatus.class)));
    }
}
