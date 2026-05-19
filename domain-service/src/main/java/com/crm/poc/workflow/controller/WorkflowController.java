package com.crm.poc.workflow.controller;

import com.crm.poc.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcess(@RequestBody StartProcessRequest request) {
        long processInstanceKey = workflowService.startProcessInstance(
                request.bpmnProcessId(), request.variables());
        return ResponseEntity.ok(Map.of(
                "processInstanceKey", processInstanceKey,
                "bpmnProcessId", request.bpmnProcessId(),
                "status", "STARTED"
        ));
    }

    @PostMapping("/tasks/{jobKey}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable long jobKey,
            @RequestBody Map<String, Object> variables) {
        workflowService.completeUserTask(jobKey, variables);
        return ResponseEntity.ok(Map.of(
                "jobKey", jobKey,
                "status", "COMPLETED"
        ));
    }

    public record StartProcessRequest(String bpmnProcessId, Map<String, Object> variables) {}
}
