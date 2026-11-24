package com.smsweb.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * One row of Admin > Family Mapping: an Active student of the current session and branch,
 * together with the family account (if any) it is linked to.
 *
 * The first constructor is used by the JPQL constructor expression in
 * FamilyMappingQueryRepository; {@link #state} is worked out afterwards in memory.
 */
@Getter
@Setter
public class FamilyMappingRowDto {

    public static final String MAPPED = "MAPPED";
    public static final String MAP = "MAP";
    public static final String MISMATCH = "MISMATCH";
    public static final String MISSING = "MISSING";

    private Long academicStudentId;
    private Long studentId;
    private String studentName;
    private String gradeSection;
    private String fatherName;
    private String srNo;
    private Long psrn;
    private String mobile1;
    private String mobile2;

    @JsonIgnore
    private Long familyAccountId;
    @JsonIgnore
    private String familyMobile;
    @JsonIgnore
    private String familyStatus;

    /** MAPPED / MAP / MISMATCH / MISSING */
    private String state;

    public FamilyMappingRowDto(Long academicStudentId, Long studentId, String studentName,
                               String gradeName, String sectionName, String fatherName,
                               String srNo, Long psrn, String mobile1, String mobile2,
                               Long familyAccountId, String familyMobile, String familyStatus) {
        this.academicStudentId = academicStudentId;
        this.studentId = studentId;
        this.studentName = studentName;
        String g = gradeName == null ? "" : gradeName.trim();
        String s = sectionName == null ? "" : sectionName.trim();
        this.gradeSection = g.isEmpty() ? s : (s.isEmpty() ? g : g + "-" + s);
        this.fatherName = fatherName;
        this.srNo = srNo;
        this.psrn = psrn;
        this.mobile1 = mobile1 == null ? null : mobile1.trim();
        this.mobile2 = mobile2 == null ? null : mobile2.trim();
        this.familyAccountId = familyAccountId;
        this.familyMobile = familyMobile;
        this.familyStatus = familyStatus;
    }
}
