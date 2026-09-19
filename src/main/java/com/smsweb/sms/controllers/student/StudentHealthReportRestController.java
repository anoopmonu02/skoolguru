package com.smsweb.sms.controllers.student;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.mobile.StudentHealthInfo;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.models.student.AcademicStudent;
import com.smsweb.sms.repositories.mobile.StudentHealthInfoRepository;
import com.smsweb.sms.services.student.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NEW, isolated REST controller backing "Student Health Report (Grade-wise)".
 * Mapped at the same flat (no class-level prefix) scheme StudentRestController
 * already uses for its AJAX endpoints (e.g. getStudentsForIdCard) — does not
 * modify that existing controller or any of its endpoints.
 *
 * Role gate matches StudentHealthReportController (the page) and the
 * sidebar's "Student Report" sub-group in base.html exactly.
 */
@RestController
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_TEACHER','ROLE_ACCOUNTENT')")
public class StudentHealthReportRestController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(StudentHealthReportRestController.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentHealthInfoRepository studentHealthInfoRepository;

    private static final Set<String> VALID_BODY_TYPES = Set.of("NORMAL", "PERSON WITH A DISABILITY");

    @CheckAccess(screen = "STUDENT_HEALTH_REPORT", type = AccessType.VIEW)
    @PostMapping("/getStudentsForHealthReport")
    public ResponseEntity<?> getStudentsForHealthReport(@RequestBody Map<String, String> requestBody, Model model) {
        log.info("Inside getStudentsForHealthReport");
        try {
            Long academicYearId = parseLong(requestBody != null ? requestBody.get("academicYearId") : null);
            Long mediumId = parseLong(requestBody != null ? requestBody.get("medium") : null);
            String bodyType = requestBody != null ? requestBody.get("bodyType") : null;
            if (academicYearId == null || mediumId == null || bodyType == null || bodyType.isBlank()) {
                return ResponseEntity.badRequest().body("Academic Year, Medium and Health are all mandatory.");
            }
            if (!VALID_BODY_TYPES.contains(bodyType)) {
                return ResponseEntity.badRequest().body("Please select a valid Health option.");
            }

            // school comes from the server-side session (BaseController), never from
            // the request body — the same IDOR-safe pattern every other school-scoped
            // read endpoint in this app already uses. academicYearId is still safe to
            // take from the client because the query below always re-scopes by this
            // session's schoolId too, so a mismatched academicYearId just returns no
            // rows instead of leaking another school's students.
            School school = (School) model.getAttribute("school");
            if (school == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to resolve current school.");
            }

            List<AcademicStudent> students = studentService.getAllStudentsByMediumAndBodyType(
                    mediumId, academicYearId, school.getId(), bodyType);
            if (students.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // One batch query for the whole class's health info instead of a
            // separate lookup per student (avoids an N+1 query pattern).
            List<Long> academicStudentIds = students.stream()
                    .map(AcademicStudent::getId)
                    .collect(Collectors.toList());
            Map<Long, StudentHealthInfo> healthByAcademicStudentId = studentHealthInfoRepository
                    .findAllByAcademicStudent_IdIn(academicStudentIds)
                    .stream()
                    .collect(Collectors.toMap(h -> h.getAcademicStudent().getId(), h -> h, (a, b) -> a));

            List<Map<String, Object>> rows = students.stream().map(as -> {
                Map<String, Object> row = new HashMap<>();
                row.put("classSrNo", as.getClassSrNo());
                row.put("gradeName", as.getGrade() != null ? as.getGrade().getGradeName() : "");
                row.put("sectionName", as.getSection() != null ? as.getSection().getSectionName() : "");
                row.put("studentName", as.getStudent().getStudentName());
                row.put("fatherName", as.getStudent().getFatherName());
                row.put("motherName", as.getStudent().getMotherName());
                row.put("address", as.getStudent().getAddress());
                row.put("mobile1", as.getStudent().getMobile1());
                row.put("mobile2", as.getStudent().getMobile2());
                row.put("bodyType", as.getStudent().getBodyType());
                row.put("bloodGroup", as.getStudent().getBloodGroup());
                row.put("pic", as.getStudent().getPic());

                StudentHealthInfo health = healthByAcademicStudentId.get(as.getId());
                row.put("height", health != null ? health.getHeight() : null);
                row.put("weight", health != null ? health.getWeight() : null);
                row.put("haveHealthIssues", health != null && Boolean.TRUE.equals(health.getHaveHealthIssues()));
                row.put("haveEyeIssue", health != null && Boolean.TRUE.equals(health.getHaveEyeIssue()));
                return row;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            log.error("Error in getStudentsForHealthReport", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
