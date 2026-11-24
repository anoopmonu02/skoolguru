package com.smsweb.sms.controllers.student;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.models.admin.AcademicYear;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.services.admin.AcademicyearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * NEW, isolated page controller for "Search Alumni" under Student Report.
 * Read-only; does not modify StudentController or any existing controller.
 *
 * Role gate matches StudentAlumniRestController (the data endpoints) and the
 * sidebar entry in base.html. ACCOUNTENT additionally needs the
 * STUDENT_SEARCH_ALUMNI permission to be granted (Admin/SuperAdmin pass automatically).
 */
@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_ACCOUNTENT')")
public class StudentAlumniController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(StudentAlumniController.class);

    @Autowired
    private AcademicyearService academicyearService;

    @CheckAccess(screen = "STUDENT_SEARCH_ALUMNI", type = AccessType.VIEW)
    @GetMapping("/search-alumni")
    public String searchAlumniPage(Model model) {
        log.info("Inside searchAlumniPage");
        School school = (School) model.getAttribute("school");
        List<AcademicYear> academicYears = academicyearService.getAllAcademiyears(school.getId());
        model.addAttribute("academicYears", academicYears);
        return "student/search-alumni";
    }
}
