package com.crm.poc.data;

import com.crm.poc.form.model.FormSchema;
import com.crm.poc.lead.model.Lead;
import com.crm.poc.user.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final InMemoryStore<Lead> leadStore;
    private final InMemoryStore<User> userStore;
    private final InMemoryStore<FormSchema> formSchemaStore;
    private final ObjectMapper objectMapper;

    public DataLoader(InMemoryStore<Lead> leadStore,
                      InMemoryStore<User> userStore,
                      InMemoryStore<FormSchema> formSchemaStore,
                      ObjectMapper objectMapper) {
        this.leadStore = leadStore;
        this.userStore = userStore;
        this.formSchemaStore = formSchemaStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        loadLeads();
        loadUsers();
        loadFormSchemas();
        log.info("Sample data loaded successfully.");
    }

    private void loadLeads() throws Exception {
        InputStream is = new ClassPathResource("data/leads.json").getInputStream();
        List<Lead> leads = objectMapper.readValue(is, new TypeReference<List<Lead>>() {});
        leadStore.saveAll(leads);
        log.info("Loaded {} leads", leads.size());
    }

    private void loadUsers() throws Exception {
        InputStream is = new ClassPathResource("data/users.json").getInputStream();
        List<User> users = objectMapper.readValue(is, new TypeReference<List<User>>() {});
        userStore.saveAll(users);
        log.info("Loaded {} users", users.size());
    }

    private void loadFormSchemas() throws Exception {
        InputStream is = new ClassPathResource("data/form-schemas.json").getInputStream();
        List<FormSchema> schemas = objectMapper.readValue(is, new TypeReference<List<FormSchema>>() {});
        formSchemaStore.saveAll(schemas);
        log.info("Loaded {} form schemas", schemas.size());
    }
}
