package com.crm.poc.workflow.service;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final ZeebeClient zeebeClient;

    public WorkflowService(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    public long startProcessInstance(String bpmnProcessId, Map<String, Object> variables) {
        var result = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("Started process instance: {} (key: {})", bpmnProcessId, result.getProcessInstanceKey());
        return result.getProcessInstanceKey();
    }

    public void completeUserTask(long jobKey, Map<String, Object> variables) {
        zeebeClient.newCompleteCommand(jobKey)
                .variables(variables)
                .send()
                .join();

        log.info("Completed task with key: {}", jobKey);
    }

    /**
     * Activate and complete the first user task for a given process instance.
     * Used when we want to programmatically advance past a user task (POC shortcut).
     */
    public void completeUserTaskByProcessKey(long processInstanceKey, Map<String, Object> variables) {
        // Wait briefly for the user task job to be created
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Activate jobs for user task type
        var activateResponse = zeebeClient.newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(10)
                .workerName("lead-workflow-controller")
                .timeout(java.time.Duration.ofSeconds(30))
                .send()
                .join();

        // Find and complete the job belonging to our process instance
        for (var job : activateResponse.getJobs()) {
            if (job.getProcessInstanceKey() == processInstanceKey) {
                zeebeClient.newCompleteCommand(job.getKey())
                        .variables(variables)
                        .send()
                        .join();
                log.info("Completed user task {} for process instance {}", job.getKey(), processInstanceKey);
                return;
            }
        }

        log.warn("No user task found for process instance {}. It may have already been completed.", processInstanceKey);
    }
}
