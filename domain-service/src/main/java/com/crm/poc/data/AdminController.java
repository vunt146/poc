package com.crm.poc.data;

import com.crm.poc.form.model.FormSchema;
import com.crm.poc.lead.model.Lead;
import com.crm.poc.user.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final InMemoryStore<Lead> leadStore;
    private final InMemoryStore<User> userStore;
    private final InMemoryStore<FormSchema> formSchemaStore;
    private final ObjectMapper objectMapper;

    public AdminController(InMemoryStore<Lead> leadStore,
                           InMemoryStore<User> userStore,
                           InMemoryStore<FormSchema> formSchemaStore,
                           ObjectMapper objectMapper) {
        this.leadStore = leadStore;
        this.userStore = userStore;
        this.formSchemaStore = formSchemaStore;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        try {
            // Clear all stores
            leadStore.clear();
            userStore.clear();
            formSchemaStore.clear();

            // Reload from JSON files
            InputStream leadsIs = new ClassPathResource("data/leads.json").getInputStream();
            List<Lead> leads = objectMapper.readValue(leadsIs, new TypeReference<List<Lead>>() {});
            leadStore.saveAll(leads);

            InputStream usersIs = new ClassPathResource("data/users.json").getInputStream();
            List<User> users = objectMapper.readValue(usersIs, new TypeReference<List<User>>() {});
            userStore.saveAll(users);

            InputStream schemasIs = new ClassPathResource("data/form-schemas.json").getInputStream();
            List<FormSchema> schemas = objectMapper.readValue(schemasIs, new TypeReference<List<FormSchema>>() {});
            formSchemaStore.saveAll(schemas);

            log.info("Data reset: {} leads, {} users, {} form schemas", leads.size(), users.size(), schemas.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Data reset thành công",
                    "leads", leads.size(),
                    "users", users.size(),
                    "formSchemas", schemas.size()
            ));
        } catch (Exception e) {
            log.error("Failed to reset data", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
