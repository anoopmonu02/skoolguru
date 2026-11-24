package com.smsweb.sms.repositories.student;

import com.smsweb.sms.dto.BulkMobileLinkRow;
import com.smsweb.sms.dto.ContactFamilyChildRow;
import com.smsweb.sms.dto.FamilyMappingRowDto;
import com.smsweb.sms.models.student.AcademicStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Read-only queries for the Family Mapping screen and the family-link follow-ups
 * (Bulk Update, Fee Submission contact change). Declared on AcademicStudent only
 * because Spring Data needs a domain type; every method is an explicit @Query.
 */
public interface FamilyMappingQueryRepository extends JpaRepository<AcademicStudent, Long> {

    String ROW_SELECT =
            "SELECT new com.smsweb.sms.dto.FamilyMappingRowDto(" +
            " a.id, s.id, s.studentName, g.gradeName, sec.sectionName, s.fatherName," +
            " a.classSrNo, s.psrn, s.mobile1, s.mobile2, fa.id, fa.mobile, fa.status) " +
            "FROM AcademicStudent a JOIN a.student s " +
            " LEFT JOIN a.grade g LEFT JOIN a.section sec LEFT JOIN s.familyAccount fa ";

    /** All Active students of one medium for the current branch + session. */
    @Query(ROW_SELECT +
            "WHERE a.school.id = :schoolId AND a.academicYear.id = :yearId AND a.medium.id = :mediumId " +
            "AND UPPER(a.status) = 'ACTIVE' AND UPPER(s.status) = 'ACTIVE' " +
            "ORDER BY g.id, sec.id, s.studentName")
    List<FamilyMappingRowDto> findRows(@Param("schoolId") Long schoolId,
                                       @Param("yearId") Long yearId,
                                       @Param("mediumId") Long mediumId);

    /** One Active enrollment, scoped to the logged-in branch + session. */
    @Query(ROW_SELECT +
            "WHERE a.id = :academicStudentId AND a.school.id = :schoolId AND a.academicYear.id = :yearId " +
            "AND UPPER(a.status) = 'ACTIVE' AND UPPER(s.status) = 'ACTIVE'")
    List<FamilyMappingRowDto> findRowByAcademicStudentId(@Param("academicStudentId") Long academicStudentId,
                                                         @Param("schoolId") Long schoolId,
                                                         @Param("yearId") Long yearId);

    /** Maps AcademicStudent uuids to Student ids: rows of [uuid, studentId]. */
    @Query("SELECT a.uuid, s.id FROM AcademicStudent a JOIN a.student s WHERE a.uuid IN :uuids")
    List<Object[]> findStudentIdsByAcademicUuids(@Param("uuids") Collection<java.util.UUID> uuids);

    /** Current link state of the given students (Student ids). */
    @Query("SELECT new com.smsweb.sms.dto.BulkMobileLinkRow(s.id, s.mobile1, fa.id, fa.mobile, fa.status) " +
            "FROM Student s LEFT JOIN s.familyAccount fa WHERE s.id IN :studentIds")
    List<BulkMobileLinkRow> findLinkRows(@Param("studentIds") Collection<Long> studentIds);

    /** Active children already on a family number: Mobile 1 equals it, or already linked to its account. */
    @Query("SELECT new com.smsweb.sms.dto.ContactFamilyChildRow(s.id, s.studentName, g.gradeName, sec.sectionName, a.school.id) " +
            "FROM AcademicStudent a JOIN a.student s LEFT JOIN a.grade g LEFT JOIN a.section sec LEFT JOIN s.familyAccount fa " +
            "WHERE UPPER(a.status) = 'ACTIVE' AND UPPER(s.status) = 'ACTIVE' " +
            "AND (s.mobile1 = :mobile OR fa.mobile = :mobile) ORDER BY s.studentName")
    List<ContactFamilyChildRow> findFamilyChildren(@Param("mobile") String mobile);

    /** Every enrollment id of a student (used to end the child's app sessions). */
    @Query("SELECT a.id FROM AcademicStudent a WHERE a.student.id = :studentId")
    List<Long> findAcademicStudentIds(@Param("studentId") Long studentId);

    /** 1 when the student has an Active enrollment in this branch + session. */
    @Query("SELECT COUNT(a) FROM AcademicStudent a JOIN a.student s WHERE s.id = :studentId " +
            "AND a.school.id = :schoolId AND a.academicYear.id = :yearId " +
            "AND UPPER(a.status) = 'ACTIVE' AND UPPER(s.status) = 'ACTIVE'")
    long countActiveEnrollment(@Param("studentId") Long studentId,
                               @Param("schoolId") Long schoolId,
                               @Param("yearId") Long yearId);
}
