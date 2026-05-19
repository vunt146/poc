package com.crm.poc.form.controller;

import com.crm.poc.form.model.FormSchema;
import com.crm.poc.form.service.FormSchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@CrossOrigin(origins = "*")
public class FormController {

    private final FormSchemaService formSchemaService;

    public FormController(FormSchemaService formSchemaService) {
        this.formSchemaService = formSchemaService;
    }

    @GetMapping("/{taskType}")
    public ResponseEntity<FormSchema> getFormSchema(@PathVariable String taskType) {
        FormSchema schema = formSchemaService.getSchemaByTaskType(taskType);
        return ResponseEntity.ok(schema);
    }

    @GetMapping
    public ResponseEntity<List<FormSchema>> listFormSchemas() {
        return ResponseEntity.ok(formSchemaService.getAllSchemas());
    }
}
