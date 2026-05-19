package com.crm.poc.form.service;

import com.crm.poc.data.InMemoryStore;
import com.crm.poc.form.model.FormSchema;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormSchemaService {

    private final InMemoryStore<FormSchema> formSchemaStore;

    public FormSchemaService(InMemoryStore<FormSchema> formSchemaStore) {
        this.formSchemaStore = formSchemaStore;
    }

    public FormSchema getSchemaByTaskType(String taskType) {
        return formSchemaStore.findBy(schema -> schema.getTaskType().equals(taskType))
                .stream()
                .findFirst()
                .orElseGet(() -> getSchemaByFormKey(taskType));
    }

    public FormSchema getSchemaByFormKey(String formKey) {
        return formSchemaStore.findBy(schema -> schema.getFormKey().equals(formKey))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy form cho task type: " + formKey));
    }

    public List<FormSchema> getAllSchemas() {
        return formSchemaStore.findAll();
    }
}
