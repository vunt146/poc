package com.crm.poc.form.model;

public class ValidationRule {

    private Integer minLength;
    private Integer maxLength;
    private Number min;
    private Number max;
    private String pattern;
    private String message;

    public ValidationRule() {}

    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    public Number getMin() { return min; }
    public void setMin(Number min) { this.min = min; }
    public Number getMax() { return max; }
    public void setMax(Number max) { this.max = max; }
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
