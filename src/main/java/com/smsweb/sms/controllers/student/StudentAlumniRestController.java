package com.smsweb.sms.controllers.student;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.services.student.StudentAlumniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * NEW, isolated REST controller backing "Search Alumni" (finds any student, Active or
 * Inactive, of the current school). Mapped at the same flat
 * (no class-level prefix) scheme StudentHealthReportRestController uses.
 * Strictly read-only. The school always comes from the server-side session, never
 * from the request, and every lookup is school-scoped.
 */
@RestController
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_ACCOUNTENT')")
public class StudentAlumniRestController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(StudentAlumniRestController.class);

    @Autowired
    private StudentAlumniService studentAlumniService;

    @Value("${student.image.storage.path}")
    private String studentImageDirectory;

    @CheckAccess(screen = "STUDENT_SEARCH_ALUMNI", type = AccessType.VIEW)
    @PostMapping("/searchAlumniData")
    public ResponseEntity<?> searchAlumniData(@RequestBody Map<String, String> requestBody, Model model) {
        log.info("Inside searchAlumniData");
        try {
            School school = (School) model.getAttribute("school");
            if (school == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to resolve current school.");
            }
            Long academicYearId = parseLong(requestBody != null ? requestBody.get("academicYearId") : null);
            String query = requestBody != null ? requestBody.get("query") : null;
            if (query != null && query.length() > 100) {
                query = query.substring(0, 100);
            }
            return ResponseEntity.ok(studentAlumniService.searchAlumni(school.getId(), academicYearId, query));
        } catch (Exception e) {
            log.error("Error in searchAlumniData", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while searching alumni.");
        }
    }

    @CheckAccess(screen = "STUDENT_SEARCH_ALUMNI", type = AccessType.VIEW)
    @GetMapping("/getAlumniProfile/{uuid}")
    public ResponseEntity<?> getAlumniProfile(@PathVariable("uuid") String uuid,
                                              @RequestParam(value = "academicYearId", required = false) String academicYearId,
                                              Model model) {
        log.info("Inside getAlumniProfile");
        try {
            School school = (School) model.getAttribute("school");
            UUID id = parseUuid(uuid);
            if (school == null || id == null) {
                return ResponseEntity.badRequest().body("Invalid request.");
            }
            Optional<Map<String, Object>> profile = studentAlumniService.buildProfile(school.getId(), id, parseLong(academicYearId));
            if (profile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumni record not found.");
            }
            return ResponseEntity.ok(profile.get());
        } catch (Exception e) {
            log.error("Error in getAlumniProfile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while loading the record.");
        }
    }

    /**
     * Alumni photo (inline view, or attachment when download=true). Behind its own
     * STUDENT_SEARCH_ALUMNI permission - deliberately NOT the STUDENT_LIST-gated
     * /student/images endpoint. The file name is read from the database (never from
     * the request), sanitised, and canonical-path checked against the image folder.
     *
     * Returns Object (ResponseEntity or, when the permission aspect denies a
     * non-JSON call, its redirect String) so a denial can never cause a ClassCastException.
     */
    @CheckAccess(screen = "STUDENT_SEARCH_ALUMNI", type = AccessType.VIEW)
    @GetMapping("/alumni-photo/{uuid}")
    public Object getAlumniPhoto(@PathVariable("uuid") String uuid,
                                 @RequestParam(value = "download", defaultValue = "false") boolean download,
                                 Model model) {
        try {
            School school = (School) model.getAttribute("school");
            UUID id = parseUuid(uuid);
            if (school == null || id == null) {
                return ResponseEntity.badRequest().build();
            }
            String fileName = studentAlumniService.findPhotoFileName(school.getId(), id).orElse(null);
            if (fileName == null) {
                return ResponseEntity.notFound().build();
            }
            String safeName = fileName.replaceAll("[^a-zA-Z0-9._ -]", "");
            if (safeName.isBlank()) {
                return ResponseEntity.notFound().build();
            }
            File base = new File(studentImageDirectory).getCanonicalFile();
            File file = new File(base, safeName).getCanonicalFile();
            if (!file.getPath().startsWith(base.getPath() + File.separator)) {
                log.warn("Alumni photo path traversal blocked: resolved={}", file.getPath());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            String lower = safeName.toLowerCase(Locale.ROOT);
            MediaType type = lower.endsWith(".png") ? MediaType.IMAGE_PNG
                    : lower.endsWith(".gif") ? MediaType.IMAGE_GIF
                    : MediaType.IMAGE_JPEG;
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok().contentType(type);
            if (download) {
                String ext = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')) : ".jpg";
                String base2 = studentAlumniService.studentNameFor(school.getId(), id)
                        .replaceAll("[^a-zA-Z0-9 _-]", "").trim().replace(' ', '_');
                if (base2.isEmpty()) base2 = "alumni";
                builder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + base2 + "_photo" + ext + "\"");
            }
            return builder.body(new FileSystemResource(file));
        } catch (IOException e) {
            log.error("Error resolving alumni photo path: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error in getAlumniPhoto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static Long parseLong(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Long.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static UUID parseUuid(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return UUID.fromString(v.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
