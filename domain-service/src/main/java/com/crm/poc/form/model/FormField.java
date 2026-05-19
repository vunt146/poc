package com.crm.poc.form.model;

import java.util.List;

public class FormField {

    private String id;
    private FieldType type;
    private String label;
    private boolean required;
    private List<FieldOption> options;
    private VisibilityCondition visibilityCondition;
    private ValidationRule validation;

    public FormField() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public List<FieldOption> getOptions() { return options; }
    public void setOptions(List<FieldOption> options) { this.options = options; }
    public VisibilityCondition getVisibilityCondition() { return visibilityCondition; }
    public void setVisibilityCondition(VisibilityCondition visibilityCondition) { this.visibilityCondition = visibilityCondition; }
    public ValidationRule getValidation() { return validation; }
    public void setValidation(ValidationRule validation) { this.validation = validation; }
}
