package com.smsweb.sms.services.student;

import com.smsweb.sms.dto.ContactChangeResult;
import com.smsweb.sms.dto.ContactFamilyChildRow;
import com.smsweb.sms.models.Users.UserEntity;
import com.smsweb.sms.models.student.FamilyAccount;
import com.smsweb.sms.models.student.Student;
import com.smsweb.sms.models.student.StudentContactChangeLog;
import com.smsweb.sms.repositories.student.FamilyAccountRepository;
import com.smsweb.sms.repositories.student.FamilyMappingQueryRepository;
import com.smsweb.sms.repositories.student.StudentContactChangeLogRepository;
import com.smsweb.sms.repositories.student.StudentRepository;
import com.smsweb.sms.services.mobile.FamilyAccountService;
import com.smsweb.sms.services.mobile.MobileRefreshTokenService;
import com.smsweb.sms.services.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Fee Submission > Update Contact, in two calls. Call 1 ({@link #check}) only checks and changes
 * nothing. Call 2 ({@link #apply}) re-runs every check and applies in ONE transaction: set
 * Mobile 1, link the family (siblings on that number too), end the child's app sessions if the
 * family changed, write the audit row. Any failure rolls everything back. The old family
 * account is never touched, and an INACTIVE target account is never activated.
 */
@Service
public class StudentContactChangeService {
    private static final Logger log = LoggerFactory.getLogger(StudentContactChangeService.class);

    private final StudentRepository studentRepository;
    private final FamilyAccountRepository familyAccountRepository;
    private final FamilyMappingQueryRepository queryRepository;
    private final StudentContactChangeLogRepository logRepository;
    private final FamilyAccountService familyAccountService;
    private final MobileRefreshTokenService refreshTokenService;
    private final UserService userService;

    public StudentContactChangeService(StudentRepository studentRepository,
                                       FamilyAccountRepository familyAccountRepository,
                                       FamilyMappingQueryRepository queryRepository,
                                       StudentContactChangeLogRepository logRepository,
                                       FamilyAccountService familyAccountService,
                                       MobileRefreshTokenService refreshTokenService,
                                       UserService userService) {
        this.studentRepository = studentRepository;
        this.familyAccountRepository = familyAccountRepository;
        this.queryRepository = queryRepository;
        this.logRepository = logRepository;
        this.familyAccountService = familyAccountService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    /** Call 1: validate and describe what would happen. Changes nothing. */
    @Transactional(readOnly = true)
    public ContactChangeResult check(Long studentId, String newMobile, Long schoolId, Long yearId) {
        try {
            ContactChangeResult early = validate(studentId, newMobile, schoolId, yearId);
            if (early != null) return early;
            Student student = studentRepository.findById(studentId).orElseThrow();
            return describe(student, newMobile.trim(), schoolId);
        } catch (Exception e) {
            log.error("Contact change check failed for student {}", studentId, e);
            return ContactChangeResult.error("Could not check this number right now. Please try again.");
        }
    }

    /** Call 2: re-check everything and apply atomically. */
    @Transactional
    public ContactChangeResult apply(Long studentId, String newMobile, Long schoolId, Long yearId) {
        ContactChangeResult early = validate(studentId, newMobile, schoolId, yearId);
        if (early != null) return early;
        String mobile = newMobile.trim();

        Student student = studentRepository.findById(studentId).orElseThrow();
        String oldMobile = student.getMobile1();
        Long oldFamilyId = student.getFamilyAccount() != null ? student.getFamilyAccount().getId() : null;

        student.setMobile1(mobile);
        UserEntity actor = userService.getLoggedInUser();
        student.setUpdatedBy(actor);
        studentRepository.save(student);

        // Links every student on this number, this child included (query auto-flushes the new mobile1).
        FamilyAccount account = familyAccountService.createIfAbsent(mobile);
        Long newFamilyId = account.getId();

        boolean linkChanged = !Objects.equals(oldFamilyId, newFamilyId);
        boolean revoked = false;
        if (linkChanged) {
            refreshTokenService.revokeAllForFamily(queryRepository.findAcademicStudentIds(studentId));
            revoked = true;
        }

        StudentContactChangeLog row = new StudentContactChangeLog();
        row.setStudentId(studentId);
        row.setSchoolId(schoolId);
        row.setAcademicYearId(yearId);
        row.setOldMobile(oldMobile);
        row.setNewMobile(mobile);
        row.setOldFamilyAccountId(oldFamilyId);
        row.setNewFamilyAccountId(newFamilyId);
        row.setSessionsRevoked(revoked);
        row.setSource("FEE_SCREEN");
        row.setChangedBy(actor != null ? actor.getUsername() : null);
        row.setChangedAt(LocalDateTime.now());
        logRepository.save(row);

        ContactChangeResult ok = new ContactChangeResult();
        ok.setSuccessMessage("Contact number: " + mobile + " updated successfully.");
        return ok;
    }

    /** Shared by both calls. Returns a ready result when the change must stop here, else null. */
    private ContactChangeResult validate(Long studentId, String newMobile, Long schoolId, Long yearId) {
        String mobile = newMobile == null ? "" : newMobile.trim();
        if (mobile.isEmpty()) return ContactChangeResult.error("Contact number cannot be empty.");
        if (!FamilyMappingService.isValidMobile(mobile)) {
            return ContactChangeResult.error("Contact number must be exactly 10 digits and contain only numbers.");
        }
        if (studentId == null || schoolId == null || yearId == null
                || queryRepository.countActiveEnrollment(studentId, schoolId, yearId) < 1) {
            return ContactChangeResult.error("Student not found in the current school and session.");
        }
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) return ContactChangeResult.error("Student not found in the current school and session.");
        if (mobile.equals(student.getMobile1() == null ? "" : student.getMobile1().trim())) {
            return ContactChangeResult.noChange("No change made. This is already the contact number.");
        }
        Optional<FamilyAccount> target = familyAccountRepository.findByMobile(mobile);
        if (target.isPresent() && !"ACTIVE".equalsIgnoreCase(target.get().getStatus())) {
            return ContactChangeResult.error("The parent app account for this number is INACTIVE, so it cannot be used. Reactivate it first.");
        }
        return null;
    }

    private ContactChangeResult describe(Student student, String mobile, Long schoolId) {
        // Children (other than this one) already on the number; names only for THIS branch.
        Map<Long, ContactFamilyChildRow> unique = new LinkedHashMap<>();
        for (ContactFamilyChildRow c : queryRepository.findFamilyChildren(mobile)) {
            if (!Objects.equals(c.getStudentId(), student.getId())) unique.putIfAbsent(c.getStudentId(), c);
        }
        List<Map<String, String>> here = new ArrayList<>();
        int other = 0;
        for (ContactFamilyChildRow c : unique.values()) {
            if (Objects.equals(c.getSchoolId(), schoolId)) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("name", c.getName());
                m.put("classSection", c.getClassSection());
                here.add(m);
            } else {
                other++;
            }
        }
        boolean accountExists = familyAccountRepository.findByMobile(mobile).isPresent();
        boolean existing = accountExists || !unique.isEmpty();

        boolean leavesOld = student.getFamilyAccount() != null
                && !mobile.equals(student.getFamilyAccount().getMobile());

        ContactChangeResult r = new ContactChangeResult();
        r.setNeedsConfirmation(true);
        r.setScenario(existing ? "EXISTING_FAMILY" : "NEW_FAMILY");
        r.setChildren(here);
        r.setFamilyChildCount(unique.size());
        r.setOtherBranchCount(other);
        StringBuilder msg = new StringBuilder();
        if (existing) {
            msg.append("This number already belongs to a family with ").append(unique.size())
               .append(unique.size() == 1 ? " other child" : " other children")
               .append(". After the change, that family's parent app will also show this child's data.");
        } else {
            msg.append("No existing family found for this number. A new parent app account will be created for it.");
        }
        if (leavesOld) {
            msg.append(" The child leaves the current family account and will be signed out of the app.");
        }
        r.setMessage(msg.toString());
        return r;
    }
}
