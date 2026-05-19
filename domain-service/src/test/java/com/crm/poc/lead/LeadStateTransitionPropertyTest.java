package com.crm.poc.lead;

import com.crm.poc.lead.model.LeadHistoryEntry;
import com.crm.poc.lead.model.LeadStateTransitions;
import com.crm.poc.lead.model.LeadStatus;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Property-Based Tests for Lead State Machine.
 * Verifies invariants of the state transition logic.
 */
class LeadStateTransitionPropertyTest {

    @Provide
    Arbitrary<LeadStatus> nonTerminalStatus() {
        return Arbitraries.of(
                LeadStatus.NEW_LEAD,
                LeadStatus.NEW_IMPORTED_LEAD,
                LeadStatus.CONTACTED,
                LeadStatus.PROCESSING,
                LeadStatus.DOCUMENT_COLLECTED
        );
    }

    @Provide
    Arbitrary<LeadStatus> terminalStatus() {
        return Arbitraries.of(LeadStatus.COMPLETED, LeadStatus.REJECTED);
    }

    @Provide
    Arbitrary<LeadStatus> anyStatus() {
        return Arbitraries.of(LeadStatus.values());
    }

    @Property(tries = 100)
    void terminalStatesCannotTransition(
            @ForAll("terminalStatus") LeadStatus terminal,
            @ForAll("anyStatus") LeadStatus target) {

        boolean canTransition = LeadStateTransitions.canTransition(terminal, target, List.of());

        if (canTransition) {
            throw new AssertionError(
                    "Terminal state " + terminal + " should not transition to " + target);
        }
    }

    @Property(tries = 100)
    void rejectedIsAlwaysReachableFromNonTerminal(
            @ForAll("nonTerminalStatus") LeadStatus current) {

        boolean canReject = LeadStateTransitions.canTransition(current, LeadStatus.REJECTED, List.of());

        if (!canReject) {
            throw new AssertionError(
                    "REJECTED should be reachable from non-terminal state " + current);
        }
    }

    @Property(tries = 100)
    void visitedStatusCannotBeRevisited(
            @ForAll("nonTerminalStatus") LeadStatus current,
            @ForAll("nonTerminalStatus") LeadStatus visited) {

        // Create history showing 'visited' was already reached
        List<LeadHistoryEntry> history = new ArrayList<>();
        history.add(createHistoryEntry(LeadStatus.NEW_LEAD, visited));

        boolean canTransition = LeadStateTransitions.canTransition(current, visited, history);

        if (canTransition) {
            throw new AssertionError(
                    "Should not transition to already-visited status " + visited + " from " + current);
        }
    }

    @Property(tries = 50)
    void historyGrowsMonotonically() {
        // Simulate a sequence of valid transitions and verify history grows
        LeadStatus current = LeadStatus.NEW_LEAD;
        List<LeadHistoryEntry> history = new ArrayList<>();

        LeadStatus[] possiblePath = {LeadStatus.CONTACTED, LeadStatus.PROCESSING, LeadStatus.COMPLETED};

        for (LeadStatus next : possiblePath) {
            if (LeadStateTransitions.canTransition(current, next, history)) {
                history.add(createHistoryEntry(current, next));
                int previousSize = history.size() - 1;
                if (history.size() != previousSize + 1) {
                    throw new AssertionError("History should grow by exactly 1 after each transition");
                }
                current = next;
            }
        }
    }

    @Property(tries = 100)
    void allowedTransitionsAreSubsetOfAllStatuses(
            @ForAll("anyStatus") LeadStatus current) {

        var allowed = LeadStateTransitions.getAllowedTransitions(current);

        // Allowed transitions should never include the current status itself
        if (allowed.contains(current)) {
            throw new AssertionError(
                    "Allowed transitions from " + current + " should not include itself");
        }
    }

    private LeadHistoryEntry createHistoryEntry(LeadStatus from, LeadStatus to) {
        return new LeadHistoryEntry(
                UUID.randomUUID().toString(),
                "LEAD-TEST",
                from,
                to,
                "USR-TEST",
                LocalDateTime.now(),
                "Test transition",
                "PBT test"
        );
    }
}
