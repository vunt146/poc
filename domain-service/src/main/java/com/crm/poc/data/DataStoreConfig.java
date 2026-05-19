package com.crm.poc.data;

import com.crm.poc.form.model.FormSchema;
import com.crm.poc.lead.model.Lead;
import com.crm.poc.user.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataStoreConfig {

    @Bean
    public InMemoryStore<Lead> leadStore() {
        return new InMemoryStore<>(Lead::getId);
    }

    @Bean
    public InMemoryStore<User> userStore() {
        return new InMemoryStore<>(User::getId);
    }

    @Bean
    public InMemoryStore<FormSchema> formSchemaStore() {
        return new InMemoryStore<>(FormSchema::getFormId);
    }
}
