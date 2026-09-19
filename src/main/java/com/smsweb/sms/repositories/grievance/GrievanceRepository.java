package com.smsweb.sms.repositories.grievance;

import com.smsweb.sms.models.grievance.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    // School-scoped - without the g.school.id check, any authenticated staff
    // member could read another school's grievance titles/descriptions for a
    // student just by guessing/incrementing an academicStudentId (there was no
    // tenant filter here at all before).
    @Query("SELECT DISTINCT g FROM Grievance g LEFT JOIN FETCH g.academicStudent astu " +
            "LEFT JOIN FETCH astu.student LEFT JOIN FETCH astu.grade LEFT JOIN FETCH astu.section " +
            "WHERE g.academicStudent.id = :academicStudentId " +
            "  AND g.school.id = :schoolId " +
            "ORDER BY g.createdAt DESC")
    List<Grievance> findAllByAcademicStudentIdAndSchool_IdOrderByCreatedAtDesc(@Param("academicStudentId") Long academicStudentId,
                                                                                @Param("schoolId") Long schoolId);

    /**
     * Dashboard "pending grievances" panel — anything due today or earlier
     * (overdue included, deliberately not restricted to due_date = today) that
     * hasn't been closed yet, scoped to the current school + academic year.
     */
    @Query("SELECT DISTINCT g FROM Grievance g LEFT JOIN FETCH g.academicStudent astu " +
            "LEFT JOIN FETCH astu.student LEFT JOIN FETCH astu.grade LEFT JOIN FETCH astu.section " +
            "WHERE g.closedAt IS NULL " +
            "  AND g.dueDate <= :today " +
            "  AND g.school.id = :schoolId " +
            "  AND g.academicYear.id = :academicYearId " +
            "ORDER BY g.dueDate ASC")
    List<Grievance> findPendingDueTodayOrOverdue(@Param("today") Date today,
                                                  @Param("schoolId") Long schoolId,
                                                  @Param("academicYearId") Long academicYearId);
}
