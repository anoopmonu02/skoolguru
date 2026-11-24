package com.smsweb.sms.dto;

import lombok.Getter;

/** One class (grade + section) of a multi-class notification with its Active student count. */
@Getter
public class ClassRecipientCount {
    private final Long gradeId;
    private final Long sectionId;
    private final String gradeName;
    private final String sectionName;
    private final long studentCount;

    public ClassRecipientCount(Long gradeId, Long sectionId, String gradeName, String sectionName, long studentCount) {
        this.gradeId = gradeId;
        this.sectionId = sectionId;
        this.gradeName = gradeName;
        this.sectionName = sectionName;
        this.studentCount = studentCount;
    }

    public String getLabel() {
        return gradeName + "-" + sectionName;
    }
}
