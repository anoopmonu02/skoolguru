package com.smsweb.sms.controllers.student;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.services.globalaccess.DropdownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * NEW, isolated page controller for "Update Student Images (Group-wise)" -
 * does not modify StudentController, StudentBulkUpdateController, or any
 * other existing controller. Same role gate as Update Student Details
 * (StudentBulkUpdateController) in this app, so the page and the REST
 * endpoints behind it (StudentImageUploadRestController) never disagree
 * about who can use this screen.
 */
@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_TEACHER')")
public class StudentImageUploadController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(StudentImageUploadController.class);

    @Autowired
    private DropdownService dropdownService;

    @CheckAccess(screen = "STUDENT_UPDATE_IMAGES", type = AccessType.VIEW)
    @GetMapping("/update-student-images")
    public String updateStudentImagesPage(Model model) {
        log.info("Inside updateStudentImagesPage");
        model.addAttribute("mediums", dropdownService.getMediums());
        model.addAttribute("grades", dropdownService.getGrades());
        model.addAttribute("sections", dropdownService.getSections());
        return "student/update-student-images";
    }
}
