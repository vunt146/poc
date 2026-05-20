package com.crm.poc.workflow.controller;

import com.crm.poc.workflow.service.WorkflowService;
import com.crm.poc.workflow.service.TasklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final TasklistService tasklistService;

    public WorkflowController(WorkflowService workflowService, TasklistService tasklistService) {
        this.workflowService = workflowService;
        this.tasklistService = tasklistService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getActiveTasks() {
        List<Map<String, Object>> tasks = tasklistService.getActiveTasks();
        return ResponseEntity.ok(tasks);
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
