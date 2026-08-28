package com.smsweb.sms.repositories.admin;

import com.smsweb.sms.models.admin.FullPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FullpaymentRepository extends JpaRepository<FullPayment, Long> {

    List<FullPayment> findAllBySchool_IdAndAcademicYear_Id(Long school_id, Long academic_id);

    // findBySchool_IdAndAcademicYear_IdAndGrade_Id — REMOVED (not kept as a
    // fallback, unlike the discount-class pattern): once medium is mandatory, a
    // grade can have 2+ FullPayment rows (one per medium), so an Optional-returning
    // grade-only query would throw IncorrectResultSizeDataAccessException as soon
    // as a school configures a second medium for the same grade. Its one caller
    // (FeeSubmissionService.getFeeDetailsBasedOnMonth) always has a resolved
    // mediumId in scope by the time it reaches this lookup, so there's no
    // legitimate defensive case for keeping it.
    Optional<FullPayment> findBySchool_IdAndAcademicYear_IdAndGrade_IdAndMedium_Id(Long school_id, Long academic_id, Long grade_id, Long medium_id);

}
