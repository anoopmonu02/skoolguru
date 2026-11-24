package com.smsweb.sms.dto;

import lombok.Getter;

/** One child already on the family number being entered in Update Contact (Fee Submission). */
@Getter
public class ContactFamilyChildRow {
    private final Long studentId;
    private final String name;
    private final String classSection;
    private final Long schoolId;

    public ContactFamilyChildRow(Long studentId, String studentName, String gradeName, String sectionName, Long schoolId) {
        this.studentId = studentId;
        this.name = studentName;
        String g = gradeName == null ? "" : gradeName.trim();
        String s = sectionName == null ? "" : sectionName.trim();
        this.classSection = g.isEmpty() ? s : (s.isEmpty() ? g : g + "-" + s);
        this.schoolId = schoolId;
    }
}
