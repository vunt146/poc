package com.crm.poc.lead.model;

public enum LeadStatus {
    NEW_LEAD(1007),
    NEW_IMPORTED_LEAD(106141),
    CONTACTED(2001),
    PROCESSING(3001),
    DOCUMENT_COLLECTED(4001),
    COMPLETED(5001),
    REJECTED(9001);

    private final int code;

    LeadStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED;
    }

    public boolean isAllocatable() {
        return this == NEW_LEAD || this == NEW_IMPORTED_LEAD;
    }
}
