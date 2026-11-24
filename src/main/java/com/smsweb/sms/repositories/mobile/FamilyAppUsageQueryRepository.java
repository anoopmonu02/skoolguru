package com.smsweb.sms.repositories.mobile;

import com.smsweb.sms.models.mobile.MobileRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Two tiny read-only queries behind the "parent mobile app" hint on the Fee Submission screen. */
public interface FamilyAppUsageQueryRepository extends JpaRepository<MobileRefreshToken, Long> {

    /** Family account id of a student (empty list when the student has no family account). */
    @Query("SELECT fa.id FROM Student s JOIN s.familyAccount fa WHERE s.id = :studentId")
    List<Long> findFamilyAccountIdOfStudent(@Param("studentId") Long studentId);

    /** Login records (refresh tokens, active or old) of every child of the family. */
    @Query("SELECT COUNT(t) FROM MobileRefreshToken t WHERE t.academicStudent.student.familyAccount.id = :familyId")
    long countTokensOfFamily(@Param("familyId") Long familyId);
}
