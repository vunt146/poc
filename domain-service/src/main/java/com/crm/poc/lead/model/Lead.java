package com.crm.poc.lead.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lead {

    private String id;
    private String customerName;
    private LeadStatus status;
    private String ownerId;
    private ProductType productType;
    private Map<String, Object> productDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LeadHistoryEntry> history = new ArrayList<>();

    public Lead() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    public Map<String, Object> getProductDetails() { return productDetails; }
    public void setProductDetails(Map<String, Object> productDetails) { this.productDetails = productDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<LeadHistoryEntry> getHistory() { return history; }
    public void setHistory(List<LeadHistoryEntry> history) { this.history = history; }
}
