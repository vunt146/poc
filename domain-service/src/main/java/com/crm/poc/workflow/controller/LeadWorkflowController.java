package com.crm.poc.workflow.controller;

import com.crm.poc.lead.model.Lead;
import com.crm.poc.lead.model.LeadStatus;
import com.crm.poc.lead.service.LeadService;
import com.crm.poc.workflow.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tích hợp workflow vào Lead actions.
 * Mỗi action = complete Camunda user task + update lead status.
 * 
 * BPMN Flow:
 * Start → [UT] Liên hệ KH → Gateway → [UT] Xử lý cơ hội → [UT] Thu thập hồ sơ → [ST] Cập nhật TT → End
 *                                  └→ End (Từ chối)
 */
@RestController
@RequestMapping("/api/workflow/lead")
@CrossOrigin(origins = "*")
public class LeadWorkflowController {

    private static final Logger log = LoggerFactory.getLogger(LeadWorkflowController.class);

    private final WorkflowService workflowService;
    private final LeadService leadService;

    public LeadWorkflowController(WorkflowService workflowService, LeadService leadService) {
        this.workflowService = workflowService;
        this.leadService = leadService;
    }

    /**
     * Bước 1: Liên hệ khách hàng.
     * - Start process instance
     * - Complete User Task "Liên hệ khách hàng"
     * - Gateway routes based on contactResult:
     *   - "INTERESTED" → advance to "Xử lý cơ hội"
     *   - "REJECTED" → end process (KH từ chối)
     * - Update lead status
     */
    @PostMapping("/{leadId}/contact")
    public ResponseEntity<Map<String, Object>> contactCustomer(
            @PathVariable String leadId,
            @RequestBody ContactRequest request) {

        Lead lead = leadService.findById(leadId);

        if (!lead.getStatus().equals(LeadStatus.NEW_LEAD) &&
            !lead.getStatus().equals(LeadStatus.NEW_IMPORTED_LEAD)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lead không ở trạng thái cho phép liên hệ",
                    "currentStatus", lead.getStatus().name()
            ));
        }

        // Start workflow process
        long processKey = workflowService.startProcessInstance("lead-lifecycle", Map.of(
                "leadId", leadId,
                "ownerId", lead.getOwnerId(),
                "contactResult", request.contactResult(),
                "action", request.action(),
                "note", request.note() != null ? request.note() : ""
        ));

        // Complete User Task "Liên hệ khách hàng" → gateway evaluates contactResult
        workflowService.completeUserTaskByProcessKey(processKey, Map.of(
                "contactResult", request.contactResult(),
                "action", request.action(),
                "note", request.note() != null ? request.note() : ""
        ));

        // Save processInstanceKey to lead for subsequent steps
        lead.setProcessInstanceKey(processKey);

        // Update lead status based on result
        if ("REJECTED".equals(request.contactResult())) {
            leadService.updateStatus(leadId, LeadStatus.REJECTED,
                    request.performedBy(), request.note(), "KH từ chối - workflow kết thúc");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "leadId", leadId,
                    "newStatus", "REJECTED",
                    "processInstanceKey", processKey,
                    "workflowEnded", true,
                    "message", "Khách hàng từ chối. Lead đã chuyển sang REJECTED. Workflow kết thúc."
            ));
        }

        leadService.updateStatus(leadId, LeadStatus.CONTACTED,
                request.performedBy(), request.note(), "Liên hệ KH qua workflow");

        log.info("Lead {} contacted. Process key: {}. Result: {}", leadId, processKey, request.contactResult());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "leadId", leadId,
                "newStatus", "CONTACTED",
                "processInstanceKey", processKey,
                "contactResult", request.contactResult(),
                "nextStep", "Xử lý cơ hội",
                "message", "Liên hệ thành công. Workflow advance → Xử lý cơ hội."
        ));
    }

    /**
     * Bước 2: Xử lý cơ hội.
     * - Complete User Task "Xử lý cơ hội"
     * - Workflow advance → "Thu thập hồ sơ"
     * - Update lead status → PROCESSING
     */
    @PostMapping("/{leadId}/process")
    public ResponseEntity<Map<String, Object>> processLead(
            @PathVariable String leadId,
            @RequestBody ProcessRequest request) {

        Lead lead = leadService.findById(leadId);

        if (!lead.getStatus().equals(LeadStatus.CONTACTED)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lead phải ở trạng thái CONTACTED để xử lý",
                    "currentStatus", lead.getStatus().name()
            ));
        }

        // Complete User Task "Xử lý cơ hội" in Camunda
        if (lead.getProcessInstanceKey() != null) {
            workflowService.completeUserTaskByProcessKey(lead.getProcessInstanceKey(), Map.of(
                    "processResult", "CONTINUE",
                    "note", request.note() != null ? request.note() : ""
            ));
        }

        leadService.updateStatus(leadId, LeadStatus.PROCESSING,
                request.performedBy(), request.note(), "Xử lý cơ hội qua workflow");

        log.info("Lead {} → PROCESSING. Workflow advance → Thu thập hồ sơ", leadId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "leadId", leadId,
                "newStatus", "PROCESSING",
                "nextStep", "Thu thập hồ sơ",
                "message", "Xử lý cơ hội thành công. Workflow advance → Thu thập hồ sơ."
        ));
    }

    /**
     * Bước 3: Thu thập hồ sơ.
     * - Complete User Task "Thu thập hồ sơ"
     * - Workflow advance → Service Task "Cập nhật trạng thái" (auto) → End
     * - Update lead status → DOCUMENT_COLLECTED → COMPLETED (service task auto)
     */
    @PostMapping("/{leadId}/collect-documents")
    public ResponseEntity<Map<String, Object>> collectDocuments(
            @PathVariable String leadId,
            @RequestBody DocumentRequest request) {

        Lead lead = leadService.findById(leadId);

        if (!lead.getStatus().equals(LeadStatus.PROCESSING)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lead phải ở trạng thái PROCESSING để thu thập hồ sơ",
                    "currentStatus", lead.getStatus().name()
            ));
        }

        // Complete User Task "Thu thập hồ sơ" in Camunda
        if (lead.getProcessInstanceKey() != null) {
            workflowService.completeUserTaskByProcessKey(lead.getProcessInstanceKey(), Map.of(
                    "documentsCollected", true,
                    "note", request.note() != null ? request.note() : "",
                    // Set variables for the next service task "lead-status-change"
                    "leadId", leadId,
                    "newStatus", "COMPLETED",
                    "changedBy", request.performedBy()
            ));
        }

        leadService.updateStatus(leadId, LeadStatus.DOCUMENT_COLLECTED,
                request.performedBy(), request.note(), "Thu thập hồ sơ qua workflow");

        log.info("Lead {} → DOCUMENT_COLLECTED. Workflow advance → Cập nhật TT (auto) → End", leadId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "leadId", leadId,
                "newStatus", "DOCUMENT_COLLECTED",
                "nextStep", "Cập nhật trạng thái (tự động) → Hoàn thành",
                "message", "Thu thập hồ sơ thành công. Workflow sẽ tự động cập nhật trạng thái và hoàn thành."
        ));
    }

    /**
     * Từ chối lead ở bất kỳ bước nào.
     * Nếu lead chưa có process → start process với REJECTED để Camunda ghi nhận.
     * Nếu lead đã có process → workflow đã chạy, chỉ update status.
     */
    @PostMapping("/{leadId}/reject")
    public ResponseEntity<Map<String, Object>> rejectLead(
            @PathVariable String leadId,
            @RequestBody RejectRequest request) {

        Lead lead = leadService.findById(leadId);

        if (lead.getStatus().isTerminal()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lead đã ở trạng thái kết thúc",
                    "currentStatus", lead.getStatus().name()
            ));
        }

        // Start workflow with REJECTED so it shows on Operate
        if (lead.getProcessInstanceKey() == null) {
            long processKey = workflowService.startProcessInstance("lead-lifecycle", Map.of(
                    "leadId", leadId,
                    "ownerId", lead.getOwnerId(),
                    "contactResult", "REJECTED",
                    "action", "REJECT",
                    "note", request.note() != null ? request.note() : ""
            ));
            workflowService.completeUserTaskByProcessKey(processKey, Map.of(
                    "contactResult", "REJECTED"
            ));
            lead.setProcessInstanceKey(processKey);
            log.info("Lead {} rejected via new workflow. Process key: {}", leadId, processKey);
        }

        leadService.updateStatus(leadId, LeadStatus.REJECTED,
                request.performedBy(), request.note(), request.reason());

        log.info("Lead {} rejected", leadId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "leadId", leadId,
                "newStatus", "REJECTED",
                "message", "Lead đã bị từ chối. Workflow kết thúc."
        ));
    }

    // Request records
    public record ContactRequest(String action, String contactResult, String note, String performedBy) {}
    public record ProcessRequest(String note, String performedBy) {}
    public record DocumentRequest(String note, String performedBy) {}
    public record CompleteRequest(String note, String performedBy) {}
    public record RejectRequest(String note, String reason, String performedBy) {}
}
