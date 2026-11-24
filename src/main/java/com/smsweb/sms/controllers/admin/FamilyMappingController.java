package com.smsweb.sms.controllers.admin;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.dto.FamilyMappingRowDto;
import com.smsweb.sms.models.admin.AcademicYear;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.services.globalaccess.DropdownService;
import com.smsweb.sms.services.student.FamilyMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin > Family Mapping. School and session always come from the logged-in session
 * (BaseController), never from the request.
 */
@Controller
@RequestMapping("/admin/family-mapping")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_STAFF')")
public class FamilyMappingController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(FamilyMappingController.class);

    private final FamilyMappingService familyMappingService;
    private final DropdownService dropdownService;

    public FamilyMappingController(FamilyMappingService familyMappingService, DropdownService dropdownService) {
        this.familyMappingService = familyMappingService;
        this.dropdownService = dropdownService;
    }

    @CheckAccess(screen = "ADMIN_FAMILY_MAPPING", type = AccessType.VIEW)
    @GetMapping
    public String page(Model model) {
        model.addAttribute("mediums", dropdownService.getMediums());
        model.addAttribute("page", "datatable");
        return "admin/family-mapping";
    }

    @CheckAccess(screen = "ADMIN_FAMILY_MAPPING", type = AccessType.VIEW)
    @GetMapping("/rows")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rows(@RequestParam Long mediumId, Model model) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            School school = (School) model.getAttribute("school");
            AcademicYear year = (AcademicYear) model.getAttribute("academicYear");
            if (school == null || year == null) {
                res.put("error", "Unable to resolve current school / session.");
                return ResponseEntity.ok(res);
            }
            List<FamilyMappingRowDto> rows = familyMappingService.getRows(school.getId(), year.getId(), mediumId);
            res.put("rows", rows);
            res.put("summary", familyMappingService.summarize(rows));
        } catch (Exception e) {
            log.error("Family mapping rows failed", e);
            res.put("error", "Unable to load students right now. Please try again.");
        }
        return ResponseEntity.ok(res);
    }

    @CheckAccess(screen = "ADMIN_FAMILY_MAPPING", type = AccessType.EDIT)
    @PostMapping("/map")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> map(@RequestBody Map<String, Object> body, Model model) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            School school = (School) model.getAttribute("school");
            AcademicYear year = (AcademicYear) model.getAttribute("academicYear");
            Long id = toLong(body.get("academicStudentId"));
            if (school == null || year == null || id == null) {
                res.put("error", "Invalid request.");
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.ok(familyMappingService.mapOne(id, school.getId(), year.getId()));
        } catch (Exception e) {
            log.error("Family mapping map failed", e);
            res.put("error", "Could not map this student right now. Please try again.");
            return ResponseEntity.ok(res);
        }
    }

    @CheckAccess(screen = "ADMIN_FAMILY_MAPPING", type = AccessType.EDIT)
    @PostMapping("/map-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mapAll(@RequestBody Map<String, Object> body, Model model) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            School school = (School) model.getAttribute("school");
            AcademicYear year = (AcademicYear) model.getAttribute("academicYear");
            Long mediumId = toLong(body.get("mediumId"));
            if (school == null || year == null || mediumId == null) {
                res.put("error", "Invalid request.");
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.ok(familyMappingService.mapAll(school.getId(), year.getId(), mediumId));
        } catch (Exception e) {
            log.error("Family mapping map-all failed", e);
            res.put("error", "Could not complete the mapping right now. Please try again.");
            return ResponseEntity.ok(res);
        }
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
