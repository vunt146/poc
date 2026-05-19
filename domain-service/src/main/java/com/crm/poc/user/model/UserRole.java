package com.crm.poc.user.model;

public enum UserRole {
    CBBH,
    TEAM_LEAD,
    BRANCH_MANAGER,
    DIVISION_DIRECTOR;

    public boolean canAllocate() {
        return this != CBBH;
    }
}
