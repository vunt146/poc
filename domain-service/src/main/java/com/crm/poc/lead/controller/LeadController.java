package com.crm.poc.lead.controller;

import com.crm.poc.lead.model.Lead;
import com.crm.poc.lead.model.LeadStatus;
import com.crm.poc.lead.service.LeadAllocationService;
import com.crm.poc.lead.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;
    private final LeadAllocationService allocationService;

    public LeadController(LeadService leadService, LeadAllocationService allocationService) {
        this.leadService = leadService;
        this.allocationService = allocationService;
    }

    @GetMapping
    public ResponseEntity<List<Lead>> getLeads(@RequestParam(required = false) String ownerId) {
        List<Lead> leads = leadService.findLeads(ownerId);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLeadDetail(@PathVariable String id) {
        Lead lead = leadService.findById(id);
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Lead> updateStatus(@PathVariable String id, @RequestBody StatusUpdateRequest request) {
        Lead updated = leadService.updateStatus(
                id, request.newStatus(), request.updatedBy(), request.note(), request.reason());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/allocate")
    public ResponseEntity<LeadAllocationService.AllocationResult> allocateLeads(
            @RequestBody AllocationRequest request) {
        LeadAllocationService.AllocationResult result = allocationService.allocate(
                request.leadIds(), request.targetUserIds(), request.requestedBy());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/allocatable")
    public ResponseEntity<List<Lead>> getAllocatableLeads(@RequestParam String ownerId) {
        List<Lead> leads = leadService.findAllocatableLeads(ownerId);
        return ResponseEntity.ok(leads);
    }

    public record StatusUpdateRequest(LeadStatus newStatus, String updatedBy, String note, String reason) {}
    public record AllocationRequest(List<String> leadIds, List<String> targetUserIds, String requestedBy) {}
}
