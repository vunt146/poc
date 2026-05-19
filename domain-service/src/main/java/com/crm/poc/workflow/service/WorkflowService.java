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
}
