package com.smsweb.sms.dto;

import lombok.Getter;

/** Link state of one student right after a Bulk Update of Mobile 1 / Mobile 2 (read-only view). */
@Getter
public class BulkMobileLinkRow {
    private final Long studentId;
    private final String mobile1;
    private final Long familyAccountId;
    private final String familyMobile;
    private final String familyStatus;

    public BulkMobileLinkRow(Long studentId, String mobile1, Long familyAccountId,
                             String familyMobile, String familyStatus) {
        this.studentId = studentId;
        this.mobile1 = mobile1 == null ? null : mobile1.trim();
        this.familyAccountId = familyAccountId;
        this.familyMobile = familyMobile;
        this.familyStatus = familyStatus;
    }

    /** Linked to an ACTIVE account whose mobile equals this student's mobile1. */
    public boolean isLinkedCorrectly() {
        return familyAccountId != null
                && "ACTIVE".equalsIgnoreCase(familyStatus)
                && mobile1 != null && mobile1.equals(familyMobile);
    }
}
