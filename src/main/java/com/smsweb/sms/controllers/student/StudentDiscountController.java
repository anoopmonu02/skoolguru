package com.smsweb.sms.controllers.student;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.exceptions.ObjectNotSaveException;
import com.smsweb.sms.exceptions.UniqueConstraintsException;
import com.smsweb.sms.models.admin.AcademicYear;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.student.AcademicStudent;
import com.smsweb.sms.models.student.StudentDiscount;
import com.smsweb.sms.models.universal.Discounthead;
import com.smsweb.sms.services.admin.AcademicyearService;
import com.smsweb.sms.services.admin.SchoolService;
import com.smsweb.sms.services.globalaccess.DropdownService;
import com.smsweb.sms.services.student.AcademicStudentService;
import com.smsweb.sms.services.student.StudentDiscountService;
import com.smsweb.sms.models.Users.UserEntity;
import com.smsweb.sms.services.universal.DiscountService;
import com.smsweb.sms.services.users.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_ACCOUNTENT','ROLE_STAFF')")
public class StudentDiscountController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(StudentDiscountController.class);


    private final StudentDiscountService studentDiscountService;
    private final DiscountService discountService;
    private final AcademicyearService academicyearService;
    private final SchoolService schoolService;
    private final AcademicStudentService academicStudentService;
    private final UserService userService;

    @Autowired
    public StudentDiscountController(StudentDiscountService studentDiscountService, DiscountService discountService, AcademicyearService academicyearService, SchoolService schoolService,
                                     AcademicStudentService academicStudentService, UserService userService){
        this.studentDiscountService = studentDiscountService;
        this.discountService = discountService;
        this.academicyearService = academicyearService;
        this.schoolService = schoolService;
        this.academicStudentService = academicStudentService;
        this.userService = userService;
    }

    @CheckAccess(screen = "STUDENT_DISCOUNT_LIST", type = AccessType.VIEW)
    @GetMapping("/stu-discount-list")
    public String discountpage(Model model){
        log.info("Inside discountpage");
        School school = getSchool(model);
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<StudentDiscount> studentDiscountList = studentDiscountService.getAllStudentDiscounts(school.getId(), academicYear.getId());
        model.addAttribute("studentDiscounts", studentDiscountList);
        model.addAttribute("hasDiscounts", !studentDiscountList.isEmpty());
        model.addAttribute("page", "datatable");
        return "student/assigneddiscount";
    }

    @CheckAccess(screen = "STUDENT_DISCOUNT_ASSIGN", type = AccessType.CREATE)
    @GetMapping("/assign-discount/add")
    public String addDiscountPage(Model model){
        log.info("Inside addDiscountPage");
        List<Discounthead> discountheads = discountService.getAllDiscountheadsExcludeSibling();
        model.addAttribute("discounts",discountheads);
        model.addAttribute("studentDiscount",new StudentDiscount());
        return "student/discountassign";
    }

    @CheckAccess(screen = "STUDENT_DISCOUNT_ASSIGN", type = AccessType.CREATE)
    @PostMapping("/assign-discount")
    public String saveStudentDiscount(@Valid @ModelAttribute("studentDiscount")StudentDiscount studentDiscount, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside saveStudentDiscount");
        if(result.hasErrors()){
            List<Discounthead> discountheads = discountService.getAllDiscountheadsExcludeSibling();
            model.addAttribute("discounts",discountheads);
            return "student/discountassign";
        }
        try{
            log.debug("studentDiscount received: id={}", studentDiscount.getId());
            School school = getSchool(model);
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            // If an id was submitted but doesn't resolve to a discount record already
            // owned by this school (a stale form, or a tampered id aimed at another
            // school's row), treat this as a brand-new record instead of letting a
            // non-null id reach studentDiscountService.save() below - JPA's
            // repository.save() merges/updates when the id is non-null, which would
            // otherwise silently overwrite another school's discount row (repointing
            // it to this school's student/discount data) even though it belongs to a
            // different tenant. Same fix already applied to StudentController#saveStudent.
            if (studentDiscount.getId() != null) {
                StudentDiscount existing = studentDiscountService.findById(studentDiscount.getId()).orElse(null);
                if (existing == null || existing.getSchool() == null || !existing.getSchool().getId().equals(school.getId())) {
                    studentDiscount.setId(null);
                }
            }
            AcademicStudent student = academicStudentService.searchStudentById(studentDiscount.getAcademicStudent().getId(), academicYear.getId(), school.getId());
            studentDiscount.setAcademicYear(academicYear);
            studentDiscount.setSchool(school);
            studentDiscount.setAcademicStudent(student);
            if (studentDiscount.getId() == null) {
                UserEntity loggedInUser = userService.getLoggedInUser();
                studentDiscount.setCreatedBy(loggedInUser);
            }
            String returnMsg = "Discount assigned successfully to: "+studentDiscount.getAcademicStudent().getStudent().getStudentName();
            if(studentDiscount.getId()!=null){
                if(!studentDiscount.getStatus().equalsIgnoreCase("Inactive")){
                    returnMsg = "Discount assigning updated successfully for: "+studentDiscount.getAcademicStudent().getStudent().getStudentName();
                }
            }
            studentDiscount.setStatus("Active");
            studentDiscountService.save(studentDiscount);
            ra.addFlashAttribute("success", returnMsg);
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", de.getLocalizedMessage());
            List<Discounthead> discountheads = discountService.getAllDiscountheadsExcludeSibling();
            model.addAttribute("discounts",discountheads);
            return "student/discountassign";
        } catch(ObjectNotSaveException oe){
            model.addAttribute("error", oe.getLocalizedMessage());
            List<Discounthead> discountheads = discountService.getAllDiscountheadsExcludeSibling();
            model.addAttribute("discounts",discountheads);
            return "student/discountassign";
        } catch(Exception e){
            model.addAttribute("error", e.getLocalizedMessage());
            List<Discounthead> discountheads = discountService.getAllDiscountheadsExcludeSibling();
            model.addAttribute("discounts",discountheads);
            return "student/discountassign";
        }
        return "redirect:/student/stu-discount-list";
    }

    // POST, not GET - a state-changing action needs to go through a method
    // Spring Security's CSRF filter actually covers (GET requests are
    // exempt from CSRF by design). The confirm-delete modal on
    // assigneddiscount.html submits a real POST form for this, carrying the
    // auto-injected _csrf token - same fix already applied to Academic-Year
    // List's and Sibling Group List's delete.
    @CheckAccess(screen = "STUDENT_DISCOUNT_DELETE", type = AccessType.DELETE)
    @PostMapping("/assign-discount/delete/{id}")
    public String deleteDiscount(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside deleteDiscount");
        try{
            School school = getSchool(model);
            String msg = studentDiscountService.deactivateStudentDiscount(id, school.getId());//deleteStudentDiscount(id);
            if(msg.equalsIgnoreCase("success")){
                ra.addFlashAttribute("success","Discount successfully removed from student");
            } else{
                ra.addFlashAttribute("error","Unable to remove mapping");
            }
        }catch(Exception e){
            ra.addFlashAttribute("error", "Error: "+e.getLocalizedMessage());
        }
        return "redirect:/student/stu-discount-list";
    }

    @CheckAccess(screen = "STUDENT_DISCOUNT_REPORT", type = AccessType.VIEW)
    @GetMapping("/stu-discount-list-session-wise")
    public String discountsessionpage(Model model){
        log.info("Inside discountsessionpage");
        School school = getSchool(model);
        List<AcademicYear> academicYears = academicyearService.getAllAcademiyears(school.getId());
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("page", "datatable");
        return "student/student-discount-session-list";
    }

    private School getSchool(Model model){
        return (School)model.getAttribute("school");
    }

}
