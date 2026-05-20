package com.crm.poc.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TasklistService {

    private static final Logger log = LoggerFactory.getLogger(TasklistService.class);

    @Value("${camunda.tasklist.url:http://tasklist:8080}")
    private String tasklistUrl;

    @Value("${camunda.tasklist.username:demo}")
    private String username;

    @Value("${camunda.tasklist.password:demo}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getActiveTasks() {
        try {
            // Step 1: Login to get session cookie
            String sessionCookie = login();
            if (sessionCookie == null) {
                log.warn("Failed to login to Tasklist, returning empty list");
                return Collections.emptyList();
            }

            // Step 2: Search for active tasks
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", sessionCookie);

            String searchBody = "{\"state\": \"CREATED\"}";
            HttpEntity<String> request = new HttpEntity<>(searchBody, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    tasklistUrl + "/v1/tasks/search",
                    HttpMethod.POST,
                    request,
                    List.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tasks = response.getBody();
                // Transform to simpler format for frontend
                return tasks.stream().map(task -> {
                    Map<String, Object> simplified = new LinkedHashMap<>();
                    simplified.put("jobKey", task.get("id"));
                    simplified.put("taskType", deriveTaskType(task));
                    simplified.put("taskDefinitionId", task.get("taskDefinitionId"));
                    simplified.put("name", task.get("name"));
                    simplified.put("processName", task.get("processName"));
                    simplified.put("assignee", task.get("assignee"));
                    simplified.put("creationDate", task.get("creationDate"));
                    simplified.put("formKey", task.get("formKey"));
                    return simplified;
                }).toList();
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching tasks from Tasklist", e);
            return Collections.emptyList();
        }
    }

    /**
     * Derive taskType from formKey by removing the "-form" suffix.
     * e.g., "lead-contact-form" -> "lead-contact"
     */
    private String deriveTaskType(Map<String, Object> task) {
        Object formKey = task.get("formKey");
        if (formKey != null) {
            String fk = formKey.toString();
            if (fk.endsWith("-form")) {
                return fk.substring(0, fk.length() - 5);
            }
            return fk;
        }
        // Fallback to taskDefinitionId
        Object taskDefId = task.get("taskDefinitionId");
        return taskDefId != null ? taskDefId.toString() : "unknown";
    }

    private String login() {
        try {
            String loginUrl = tasklistUrl + "/api/login?username=" + username + "&password=" + password;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null && !cookies.isEmpty()) {
                // Extract session cookie
                return cookies.stream()
                        .filter(c -> c.contains("TASKLIST-SESSION"))
                        .map(c -> c.split(";")[0])
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to login to Tasklist", e);
            return null;
        }
    }
}
