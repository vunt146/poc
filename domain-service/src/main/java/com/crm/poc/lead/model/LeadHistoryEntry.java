package com.crm.poc.lead.model;

import java.time.LocalDateTime;

public class LeadHistoryEntry {

    private String id;
    private String leadId;
    private LeadStatus previousStatus;
    private LeadStatus newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String note;
    private String reason;

    public LeadHistoryEntry() {}

    public LeadHistoryEntry(String id, String leadId, LeadStatus previousStatus, LeadStatus newStatus,
                            String changedBy, LocalDateTime changedAt, String note, String reason) {
        this.id = id;
        this.leadId = leadId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.note = note;
        this.reason = reason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLeadId() { return leadId; }
    public void setLeadId(String leadId) { this.leadId = leadId; }
    public LeadStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(LeadStatus previousStatus) { this.previousStatus = previousStatus; }
    public LeadStatus getNewStatus() { return newStatus; }
    public void setNewStatus(LeadStatus newStatus) { this.newStatus = newStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
