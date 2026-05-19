package com.crm.poc.form.model;

import java.util.List;

public class FormSchema {

    private String formId;
    private String formKey;
    private String taskType;
    private String title;
    private String description;
    private List<FormField> fields;

    public FormSchema() {}

    public String getFormId() { return formId; }
    public void setFormId(String formId) { this.formId = formId; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<FormField> getFields() { return fields; }
    public void setFields(List<FormField> fields) { this.fields = fields; }
}
