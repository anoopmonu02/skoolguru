package com.smsweb.sms.services.student;

import com.smsweb.sms.models.student.AcademicStudent;
import com.smsweb.sms.models.student.Student;
import com.smsweb.sms.repositories.student.AcademicStudentRepository;
import com.smsweb.sms.repositories.student.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * NEW, isolated, READ-ONLY service behind the "Search Alumni" screen.
 *
 * Despite the name, the screen finds ANY student of the school - Active or
 * Inactive - for a chosen session. Nothing here writes to the database, and
 * nothing here changes StudentService or the shared list/mapper code: the
 * profile is assembled by its own builder so no existing screen is affected.
 *
 * Every lookup is scoped by the caller's school id (taken from the server-side
 * session by the controller, never from the request).
 */
@Service
public class StudentAlumniService {

    private static final Logger log = LoggerFactory.getLogger(StudentAlumniService.class);
    private static final int MAX_RESULTS = 30;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Autowired
    private AcademicStudentRepository academicStudentRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Search students of one school enrolled in ONE session (any status: Active or
     * Inactive). Matches (case-insensitive, contains) on student / father / mother
     * name, class SR no and PSRN. Returns at most MAX_RESULTS slim rows by name.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchAlumni(Long schoolId, Long academicYearId, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (academicYearId == null || q.isEmpty()) {
            return new ArrayList<>();
        }

        // One enrolment row per student for this session (both statuses)
        List<AcademicStudent> enrolments = new ArrayList<>(
                academicStudentRepository.findAllBySchool_IdAndAcademicYear_IdAndStatus(schoolId, academicYearId, AcademicStudent.STATUS_ACTIVE));
        enrolments.addAll(
                academicStudentRepository.findAllBySchool_IdAndAcademicYear_IdAndStatus(schoolId, academicYearId, AcademicStudent.STATUS_INACTIVE));

        Map<Long, AcademicStudent> byStudent = new LinkedHashMap<>();
        for (AcademicStudent as : enrolments) {
            if (as.getStudent() == null) continue;
            Long sid = as.getStudent().getId();
            AcademicStudent current = byStudent.get(sid);
            if (current == null || compareRecency(as, current) > 0) {
                byStudent.put(sid, as);
            }
        }

        List<AcademicStudent> matched = new ArrayList<>();
        for (AcademicStudent as : byStudent.values()) {
            if (matches(as, q)) matched.add(as);
        }
        matched.sort(Comparator.comparing(
                (AcademicStudent a) -> safe(a.getStudent().getStudentName()).toLowerCase(Locale.ROOT)));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (AcademicStudent as : matched) {
            if (rows.size() >= MAX_RESULTS) break;
            Student s = as.getStudent();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uuid", s.getUuid() != null ? s.getUuid().toString() : "");
            row.put("studentName", safe(s.getStudentName()));
            row.put("fatherName", safe(s.getFatherName()));
            row.put("motherName", safe(s.getMotherName()));
            row.put("psrn", s.getPsrn());
            row.put("classSrNo", safe(as.getClassSrNo()));
            row.put("lastSession", as.getAcademicYear() != null ? safe(as.getAcademicYear().getSessionFormat()) : "");
            row.put("lastClass", classLabel(as));
            row.put("active", isActive(s, as));
            row.put("hasPhoto", s.getPic() != null && !s.getPic().isBlank());
            rows.add(row);
        }
        return rows;
    }

    /**
     * Full record-sheet payload for one student of this school (Active or Inactive),
     * or Optional.empty() when the uuid is unknown for this school. academicYearId
     * (optional) picks the session whose class / SR no headline the sheet; when
     * absent or not enrolled, the latest enrolment is used.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> buildProfile(Long schoolId, UUID uuid, Long academicYearId) {
        Optional<Student> found = studentRepository.findByUuidAndSchool_Id(uuid, schoolId);
        if (found.isEmpty()) return Optional.empty();
        Student s = found.get();

        List<AcademicStudent> history = new ArrayList<>(
                academicStudentRepository.findAllByStudent_IdAndSchool_Id(s.getId(), schoolId));
        history.sort(this::compareRecency);

        Map<String, Object> p = new LinkedHashMap<>();
        p.put("uuid", s.getUuid() != null ? s.getUuid().toString() : "");
        p.put("studentName", safe(s.getStudentName()));
        p.put("fatherName", safe(s.getFatherName()));
        p.put("motherName", safe(s.getMotherName()));
        p.put("psrn", s.getPsrn());
        p.put("registrationNo", safe(s.getRegistrationNo()));
        p.put("dob", s.getDob() != null ? s.getDob().format(DATE_FMT) : "");
        p.put("gender", safe(s.getGender()));
        p.put("religion", safe(s.getReligion()));
        p.put("category", s.getCategory() != null ? safe(s.getCategory().getCategoryName()) : "");
        p.put("bloodGroup", safe(s.getBloodGroup()));
        p.put("address", safe(s.getAddress()));
        p.put("mobile1", safe(s.getMobile1()));
        p.put("mobile2", safe(s.getMobile2()));
        p.put("aadharNo", safe(s.getAadharNo()));
        p.put("tcNo", safe(s.getTcNo()));
        p.put("removalCause", safe(s.getRemovalCause()));
        p.put("passingYear", s.getPassingYear());
        p.put("hasPhoto", s.getPic() != null && !s.getPic().isBlank());

        AcademicStudent focus = null;      // the session chosen on the screen
        AcademicStudent lastInactive = null;
        List<Map<String, Object>> journey = new ArrayList<>();
        for (AcademicStudent a : history) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("session", a.getAcademicYear() != null ? safe(a.getAcademicYear().getSessionFormat()) : "");
            step.put("classLabel", classLabel(a));
            step.put("classSrNo", safe(a.getClassSrNo()));
            step.put("rollNo", safe(a.getRollNo()));
            step.put("status", safe(a.getStatus()));
            journey.add(step);
            if (AcademicStudent.STATUS_INACTIVE.equalsIgnoreCase(a.getStatus())) lastInactive = a;
            if (academicYearId != null && a.getAcademicYear() != null
                    && academicYearId.equals(a.getAcademicYear().getId())) {
                focus = a;
            }
        }
        if (focus == null && !history.isEmpty()) focus = history.get(history.size() - 1);

        boolean active = isActive(s, lastInactive != null ? lastInactive : focus);
        p.put("active", active);
        p.put("journey", journey);
        p.put("lastSession", focus != null && focus.getAcademicYear() != null ? safe(focus.getAcademicYear().getSessionFormat()) : "");
        p.put("lastClass", focus != null ? classLabel(focus) : "");
        p.put("classSrNo", focus != null ? safe(focus.getClassSrNo()) : "");
        p.put("medium", focus != null && focus.getMedium() != null ? safe(focus.getMedium().getMediumName()) : "");
        p.put("exitSession", lastInactive != null && lastInactive.getAcademicYear() != null ? safe(lastInactive.getAcademicYear().getSessionFormat()) : "");
        p.put("exitClass", lastInactive != null ? classLabel(lastInactive) : "");
        return Optional.of(p);
    }

    /** Photo file name for a student of this school (any status), or empty. */
    @Transactional(readOnly = true)
    public Optional<String> findPhotoFileName(Long schoolId, UUID uuid) {
        Optional<Student> found = studentRepository.findByUuidAndSchool_Id(uuid, schoolId);
        if (found.isEmpty()) return Optional.empty();
        String pic = found.get().getPic();
        if (pic == null || pic.isBlank()) return Optional.empty();
        return Optional.of(pic);
    }

    /** Display name for a download file: "<student name>" made filesystem-safe. */
    @Transactional(readOnly = true)
    public String studentNameFor(Long schoolId, UUID uuid) {
        return studentRepository.findByUuidAndSchool_Id(uuid, schoolId)
                .map(Student::getStudentName).orElse("alumni");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean matches(AcademicStudent as, String q) {
        Student s = as.getStudent();
        return contains(s.getStudentName(), q)
                || contains(s.getFatherName(), q)
                || contains(s.getMotherName(), q)
                || contains(s.getPsrn() != null ? String.valueOf(s.getPsrn()) : null, q)
                || contains(as.getClassSrNo(), q);
    }

    private static boolean contains(String value, String lowerQ) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerQ);
    }

    /** Student is "active" unless the Student row or the given enrolment says Inactive. */
    private static boolean isActive(Student s, AcademicStudent enrolment) {
        if (Student.STATUS_INACTIVE.equalsIgnoreCase(s.getStatus())) return false;
        return enrolment == null || !AcademicStudent.STATUS_INACTIVE.equalsIgnoreCase(enrolment.getStatus());
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static String classLabel(AcademicStudent a) {
        String g = a.getGrade() != null ? safe(a.getGrade().getGradeName()) : "";
        String sec = a.getSection() != null ? safe(a.getSection().getSectionName()) : "";
        return (g + (sec.isEmpty() ? "" : " - " + sec)).trim();
    }

    /** Orders enrolments oldest -> newest by academic-year start date, then id. */
    private int compareRecency(AcademicStudent a, AcademicStudent b) {
        LocalDate da = a.getAcademicYear() != null ? a.getAcademicYear().getStartDate() : null;
        LocalDate db = b.getAcademicYear() != null ? b.getAcademicYear().getStartDate() : null;
        if (da != null && db != null) {
            int c = da.compareTo(db);
            if (c != 0) return c;
        } else if (da != null) {
            return 1;
        } else if (db != null) {
            return -1;
        }
        Long ia = a.getId() != null ? a.getId() : 0L;
        Long ib = b.getId() != null ? b.getId() : 0L;
        return ia.compareTo(ib);
    }
}
