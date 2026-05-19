package com.crm.poc.workflow.worker;

import com.crm.poc.lead.model.LeadStatus;
import com.crm.poc.lead.service.LeadAllocationService;
import com.crm.poc.lead.service.LeadService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LeadJobWorkers {

    private static final Logger log = LoggerFactory.getLogger(LeadJobWorkers.class);

    private final LeadService leadService;
    private final LeadAllocationService allocationService;

    public LeadJobWorkers(LeadService leadService, LeadAllocationService allocationService) {
        this.leadService = leadService;
        this.allocationService = allocationService;
    }

    @JobWorker(type = "lead-status-change")
    public Map<String, Object> handleStatusChange(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String leadId = (String) variables.get("leadId");
        String newStatusStr = (String) variables.get("newStatus");
        String changedBy = (String) variables.get("changedBy");
        String note = (String) variables.getOrDefault("note", "");

        log.info("Job Worker: lead-status-change for lead {} to {}", leadId, newStatusStr);

        LeadStatus newStatus = LeadStatus.valueOf(newStatusStr);
        leadService.updateStatus(leadId, newStatus, changedBy, note, "Workflow triggered");

        return Map.of("statusUpdated", true, "leadId", leadId, "newStatus", newStatusStr);
    }

    @JobWorker(type = "lead-allocation")
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleAllocation(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        List<String> leadIds = (List<String>) variables.get("leadIds");
        List<String> targetUserIds = (List<String>) variables.get("targetUserIds");
        String requestedBy = (String) variables.get("requestedBy");

        log.info("Job Worker: lead-allocation for {} leads to {} users", leadIds.size(), targetUserIds.size());

        LeadAllocationService.AllocationResult result = allocationService.allocate(leadIds, targetUserIds, requestedBy);

        return Map.of(
                "allocationSuccess", result.success(),
                "totalAllocated", result.totalLeadsAllocated()
        );
    }

    @JobWorker(type = "send-notification")
    public void handleNotification(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String message = (String) variables.getOrDefault("message", "Notification");
        log.info("Job Worker: send-notification - {}", message);
        // POC: just log the notification
    }
}
