package com.smsweb.sms.controllers.admin;

import com.smsweb.sms.config.permission.CheckAccess;
import com.smsweb.sms.models.permission.AccessType;
import com.smsweb.sms.config.AcademicYearHolder;
import com.smsweb.sms.config.SchoolHolder;
import com.smsweb.sms.controllers.BaseController;
import com.smsweb.sms.exceptions.ObjectNotDeleteException;
import com.smsweb.sms.exceptions.ObjectNotSaveException;
import com.smsweb.sms.exceptions.UniqueConstraintsException;
import com.smsweb.sms.models.Users.Employee;
import com.smsweb.sms.models.Users.Roles;
import com.smsweb.sms.models.Users.UserEntity;
import com.smsweb.sms.models.admin.*;
import com.smsweb.sms.models.universal.Discounthead;
import com.smsweb.sms.models.universal.Feehead;
import com.smsweb.sms.models.universal.Grade;
import com.smsweb.sms.models.universal.MonthMaster;
import com.smsweb.sms.models.universal.Medium;
import com.smsweb.sms.repositories.users.RoleRepository;
import com.smsweb.sms.services.Employee.EmployeeService;
import com.smsweb.sms.services.admin.*;
import com.smsweb.sms.services.universal.*;
import com.smsweb.sms.services.users.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_STAFF')")
public class GlobalController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(GlobalController.class);


    private final AcademicyearService academicyearService;
    private final MonthmappingService monthmappingService;
    private final SchoolService schoolService;
    private final MonthMasterService monthMasterService;
    private final FeedateService feedateService;
    private final FineService fineService;
    private final FineheadService fineheadService;
    private final FeeclassmapService feeclassmapService;
    private final FeemonthmapService feemonthmapService;
    private final FeeheadService feeheadService;
    private final DiscountService discountService;
    private final GradeService gradeService;
    private final DiscountclassmapService discountclassmapService;
    private final MediumService mediumService;
    private final DiscountmonthmapService discountmonthmapService;
    private final FullpaymentService fullpaymentService;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final RoleRepository roleRepository;

    private final AcademicYearHolder academicYearHolder;
    private final SchoolHolder schoolHolder;

    private final HolidayService holidayService;
    private final ExaminationService examinationService;

    @Autowired
    public GlobalController(AcademicyearService academicyearService, SchoolService schoolService, MonthmappingService monthmappingService, MonthMasterService monthMasterService,
                            FeedateService feedateService, FineService fineService, FineheadService fineheadService, FeeclassmapService feeclassmapService,
                            FeeheadService feeheadService, GradeService gradeService, FeemonthmapService feemonthmapService, DiscountclassmapService discountclassmapService, MediumService mediumService,
                            DiscountService discountService, DiscountmonthmapService discountmonthmapService, FullpaymentService fullpaymentService, UserService userService, EmployeeService employeeService, RoleRepository roleRepository, AcademicYearHolder academicYearHolder, SchoolHolder schoolHolder, HolidayService holidayService, ExaminationService examinationService){
        this.academicyearService = academicyearService;
        this.schoolService = schoolService;
        this.monthmappingService = monthmappingService;
        this.monthMasterService = monthMasterService;
        this.feedateService = feedateService;
        this.fineService = fineService;
        this.fineheadService = fineheadService;
        this.feeclassmapService = feeclassmapService;
        this.feeheadService = feeheadService;
        this.gradeService = gradeService;
        this.feemonthmapService = feemonthmapService;
        this.discountclassmapService = discountclassmapService;
        this.mediumService = mediumService;
        this.discountService = discountService;
        this.discountmonthmapService = discountmonthmapService;
        this.fullpaymentService = fullpaymentService;
        this.userService = userService;
        this.employeeService = employeeService;
        this.roleRepository = roleRepository;
        this.academicYearHolder = academicYearHolder;
        this.schoolHolder = schoolHolder;
        this.holidayService = holidayService;
        this.examinationService = examinationService;
    }

    /********************************   Academic year Code starts here   ************************************/

    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.VIEW)
    @GetMapping("/academicyear")
    public String academciyear(Model model){
        log.info("Inside academciyear");
        //Get data of school when loggedin
        List<AcademicYear> academicYears;
        School school = (School)model.getAttribute("school");
        if(isSuperAdminLoggedIn()){
            academicYears  = academicyearService.getAllAcademicYear();
        }
        else{
            academicYears = academicyearService.getAllAcademiyears(school.getId());
        }
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("hasAcademicyears", !academicYears.isEmpty());

        // Which single Academic Year row is "the active session" for each school
        // shown in this list - i.e. the exact row setAcademicYearInModel()
        // (BaseController) resolves via AcademicyearService.getCurrentAcademicYear
        // (highest-id row with status "active" for that school). Reusing that same
        // service method per distinct school here (instead of re-deriving the rule)
        // means the list's live-session dot can never drift from what actually
        // drives the top-bar year badge and every other "current year" lookup in
        // the app. For a normal (non-superadmin) view this list only has one
        // school, so it's a single extra indexed query; superadmin's cross-school
        // list does one per distinct school, which is at most a handful of rows.
        Set<Long> activeAcademicYearIds = academicYears.stream()
                .map(ay -> ay.getSchool() != null ? ay.getSchool().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .map(academicyearService::getCurrentAcademicYear)
                .filter(Objects::nonNull)
                .map(AcademicYear::getId)
                .collect(Collectors.toSet());
        model.addAttribute("activeAcademicYearIds", activeAcademicYearIds);

        model.addAttribute("page", "datatable");
        return "admin/academicyear";
    }

    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.CREATE)
    @GetMapping("/academicyear/add")
    public String addAcademicyearForm(Model model){
        log.info("Inside addAcademicyearForm");
        model.addAttribute("academicyear", new AcademicYear());
        if(isSuperAdminLoggedIn()){
            model.addAttribute("superUserLogin", true);
            model.addAttribute("schools", schoolService.getAllSchools());
        }
        else if(isAdminLogin()){
            model.addAttribute("adminLogin", true);
            model.addAttribute("school", employeeService.getLoggedInEmployeeSchool());
        }
        return "admin/add-academicyear";
    }

    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.CREATE)
    @PostMapping("/academicyear")
    public String saveAcademicYear(@Valid @ModelAttribute("academicyear") AcademicYear academicYear,
                                   BindingResult result, Model model, RedirectAttributes ra) {
        log.info("Inside saveAcademicYear");
        if (result.hasErrors()) {
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/add-academicyear";
        }

        try {
            // Only a super-admin may pick which school this Academic Year
            // belongs to. For everyone else, never trust the submitted
            // school.id - the "hidden" school field on this form is just as
            // editable via browser devtools as any visible one - always use
            // the caller's own session-derived school instead.
            School school;
            if (isSuperAdminLoggedIn()) {
                if (academicYear.getSchool() == null || academicYear.getSchool().getId() == null) {
                    throw new IllegalArgumentException("School selection is mandatory.");
                }
                school = schoolService.getSchoolById(academicYear.getSchool().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid School ID."));
            } else {
                school = (School) model.getAttribute("school");
                if (school == null) {
                    throw new IllegalArgumentException("School selection is mandatory.");
                }
            }

            academicYear.setSchool(school);
            // Save AcademicYear
            academicyearService.save(academicYear);
            ra.addFlashAttribute("success", "Academic year - " + academicYear.getSessionFormat() + " saved successfully.");
        } catch (DataIntegrityViolationException de) {
            de.printStackTrace();
            model.addAttribute("error", "Duplicate entry '" + academicYear.getSessionFormat() + "' for Academic Year.");
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/add-academicyear";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/add-academicyear";
        }

        return "redirect:/admin/academicyear";
    }


    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.EDIT)
    @GetMapping("/academicyear/edit/{id}")
    public String editAcademicYearPage(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editAcademicYearPage");
        AcademicYear academicYear = academicyearService.getAcademicyearById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid academic-year Id:" + id));
        // Cross-tenant IDOR guard: a non-super-admin must only ever see
        // their own school's academic years, however the id in the URL was
        // reached (typed, bookmarked, or guessed).
        if (!isSuperAdminLoggedIn()) {
            School sessionSchool = (School) model.getAttribute("school");
            if (sessionSchool == null || academicYear.getSchool() == null
                    || !academicYear.getSchool().getId().equals(sessionSchool.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Academic Year.");
                return "redirect:/admin/academicyear";
            }
        }
        model.addAttribute("academicyear", academicYear);
        if(isSuperAdminLoggedIn()){
            model.addAttribute("superUserLogin", true);
            model.addAttribute("schools", schoolService.getAllSchools());
        }
        else if(isAdminLogin()){
            model.addAttribute("adminLogin", true);
            model.addAttribute("school", employeeService.getLoggedInEmployeeSchool());
        }
        return "admin/edit-academicyear";
    }

    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.EDIT)
    @PostMapping("/academicyear/{id}")
    public String updateAcademicYear(@PathVariable("id") Long id, @Valid @ModelAttribute("academicyear") AcademicYear academicYear,
                                 BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside updateAcademicYear");
        if(result.hasErrors()){
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/edit-academicyear";
        }
        try{
            // edit-academicyear.html has no hidden "id" field, so the bound
            // academicYear object never carried an id - academicyearService
            // .save() then inserted a brand-new row on every "Update" click
            // instead of updating the one at this URL. Setting it explicitly
            // from the (now ownership-verified) path variable below fixes
            // that alongside the access-control check.
            School school;
            if (isSuperAdminLoggedIn()) {
                if (academicYear.getSchool() == null || academicYear.getSchool().getId() == null) {
                    throw new IllegalArgumentException("School selection is mandatory.");
                }
                school = schoolService.getSchoolById(academicYear.getSchool().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid School ID."));
            } else {
                // Never trust the submitted school.id, and confirm the
                // record at this id already belongs to the caller before
                // touching it - the {id} path segment is just as
                // attacker-controlled as any form field.
                AcademicYear existing = academicyearService.getAcademicyearById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid academic-year Id:" + id));
                school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Academic Year.");
                    return "redirect:/admin/academicyear";
                }
            }

            academicYear.setId(id);
            academicYear.setSchool(school);
            academicYear.setUpdatedBy(userService.getLoggedInUser());
            academicyearService.save(academicYear);
            ra.addFlashAttribute("success","Academic year - "+academicYear.getSessionFormat()+ " Updated successfully.");
        }catch(DataIntegrityViolationException de){
            de.printStackTrace();
            model.addAttribute("error", "Duplicate entry '"+ academicYear.getSessionFormat() +"' for Academic-Year.");
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/edit-academicyear";
        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schools", schoolService.getAllSchools());
            return "admin/edit-academicyear";
        }

        return "redirect:/admin/academicyear";
    }

    // Was a @GetMapping - a state-changing action reachable by plain GET
    // bypasses Spring Security's CSRF check entirely (CSRF only guards
    // POST/PUT/PATCH/DELETE). Switched to POST; the List page's confirmation
    // modal now submits a real form instead of navigating via GET, same fix
    // already applied to Employee/Student delete.
    @CheckAccess(screen = "ADMIN_ACYEAR", type = AccessType.DELETE)
    @PostMapping("/academicyear/delete/{id}")
    public String deleteAcademicYear(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        log.info("Inside deleteAcademicYear");
        try {
            if (!isSuperAdminLoggedIn()) {
                AcademicYear existing = academicyearService.getAcademicyearById(id).orElse(null);
                if (existing == null) {
                    ra.addFlashAttribute("error", "Academic year not found.");
                    return "redirect:/admin/academicyear";
                }
                School sessionSchool = (School) model.getAttribute("school");
                if (sessionSchool == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(sessionSchool.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Academic Year.");
                    return "redirect:/admin/academicyear";
                }
            }
            String result = academicyearService.delete(id);
            if ("success".equals(result)) {
                ra.addFlashAttribute("success", "Academic year deleted successfully.");
            } else {
                ra.addFlashAttribute("error", "Failed to delete academic year: " + result);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting academic year: " + e.getMessage());
        }
        return "redirect:/admin/academicyear";
    }

    /********************************   Month-Mapping Code starts here   ************************************/

    @CheckAccess(screen = "ADMIN_MONTH_MAP", type = AccessType.VIEW)
    @GetMapping("/month-mapping")
    public String getMonthmappings(Model model){
        log.info("Inside getMonthmappings");
        //Get data of school and academicyear when loggedin
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<MonthMapping> monthmappings = monthmappingService.getAllMonthMapping(academicYear.getId(), school.getId());
        // Required for base.html to load the DataTables/Buttons/export JS
        // bundle (see the page=='datatable' vs th:unless split there) - this
        // page's list now uses initListDataTable() same as Employee/Student,
        // which is undefined without this flag. Missing this was the actual
        // cause of "export buttons missing" and the pagination footer being
        // stuck at "Showing 0 of 0" (initListDataTable() threw a
        // ReferenceError before it could touch either).
        model.addAttribute("page", "datatable");
        model.addAttribute("monthmappings", monthmappings);
        model.addAttribute("hasMonthMappings", !monthmappings.isEmpty());
        return "admin/monthmapping";
    }

    @CheckAccess(screen = "ADMIN_MONTH_MAP", type = AccessType.CREATE)
    @GetMapping("/month-mapping/add")
    public String getAddMonthMappingForm(Model model){
        log.info("Inside getAddMonthMappingForm");
        List<MonthMaster> months = monthMasterService.getAllMonths();
        model.addAttribute("months", months);
        model.addAttribute("monthMapping", new MonthMapping());
        model.addAttribute("hasMonths", !months.isEmpty());
        /*List<Integer> numbers = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());*/
        //model.addAttribute("numbers", numbers);
        return "admin/add-month-mapping";
    }


    @CheckAccess(screen = "ADMIN_MONTH_MAP", type = AccessType.CREATE)
    @PostMapping("/month-mapping")
    public String saveMonthMapping(@RequestParam("monthMaster") Long monthMaster, RedirectAttributes redirectAttributes, Model model){
        log.info("Inside saveMonthMapping - monthMasterId={}", monthMaster);
        MonthMaster selectedMonth = monthMasterService.getMonthById(monthMaster).get();
        List<MonthMaster> months = monthMasterService.getAllMonths();
        try{
            if(selectedMonth!=null){

                School school = (School)model.getAttribute("school");
                AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
                String msg = monthmappingService.save(selectedMonth, academicYear, school);
                if(msg.equalsIgnoreCase("success")){
                    redirectAttributes.addFlashAttribute("success","Month mapping generated for this academic year-"+academicYear.getSessionFormat());
                }
                else{
                    model.addAttribute("months", months);
                    model.addAttribute("monthMapping", new MonthMapping());
                    return "admin/add-month-mapping";
                }
            }
        }catch(RuntimeException re){
            model.addAttribute("months", months);
            model.addAttribute("monthMapping", new MonthMapping());
            model.addAttribute("error","Error in saving: "+re.getMessage());
            re.printStackTrace();
            return "admin/add-month-mapping";
        }catch(Exception e){
            model.addAttribute("months", months);
            model.addAttribute("monthMapping", new MonthMapping());
            model.addAttribute("error","Error in saving: "+e.getMessage());
            e.printStackTrace();
            return "admin/add-month-mapping";
        }

        return "redirect:/admin/month-mapping";
    }

    /********************************   Fee Date Code starts here   ************************************/

    @CheckAccess(screen = "ADMIN_FEEDATE", type = AccessType.VIEW)
    @GetMapping("/feedate")
    public String getFeeDate(Model model){
        log.info("Inside getFeeDate");
        //Get data of school and academicyear when loggedin
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<FeeDate> feeDateList = feedateService.getAllFeeDates(academicYear.getId(), school.getId());
        model.addAttribute("feedates", feeDateList);
        model.addAttribute("isFeeDates", !feeDateList.isEmpty());
        // Needed for the inline "quick add" panel's th:object="${feedate}"
        // binding on this same list page (same pattern as universal/section()).
        model.addAttribute("feedate", new FeeDate());
        model.addAttribute("months", monthMasterService.getAllMonths());
        return "admin/feedate";
    }

    @CheckAccess(screen = "ADMIN_FEEDATE", type = AccessType.CREATE)
    @GetMapping("/feedate/add")
    public String getAddFeeDateForm(Model model){
        log.info("Inside getAddFeeDateForm");
        model.addAttribute("feedate", new FeeDate());
        model.addAttribute("months", monthMasterService.getAllMonths());
        return "admin/add-feedate";
    }

    @CheckAccess(screen = "ADMIN_FEEDATE", type = AccessType.CREATE)
    @PostMapping("/feedate")
    public String save(@Valid @ModelAttribute("feedate")FeeDate feedate, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside save");
        if(result.hasErrors()){
            model.addAttribute("months", monthMasterService.getAllMonths());
            // Was the raw FieldError object, not a message string - harmless
            // while nothing rendered it, but unsafe to inline into JS (Thymeleaf
            // has to serialize an arbitrary object). Every other branch below
            // already puts a plain String here; matched that so the toastr this
            // page's script now adds can display it safely.
            model.addAttribute("error", result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : "Please check the highlighted fields.");
            return "admin/add-feedate";
        }
        try{
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            feedate.setAcademicYear(academicYear);
            feedate.setSchool(school);
            feedate.setCreatedBy(userService.getLoggedInUser());
            feedateService.save(feedate);
            redirectAttributes.addFlashAttribute("success","Fee Date saved successfully for: "+feedate.getMonthMaster().getMonthName());
        }catch(DataIntegrityViolationException de){
            model.addAttribute("error", "Duplicate entry for "+feedate.getMonthMaster().getMonthName());
            model.addAttribute("months", monthMasterService.getAllMonths());
            de.printStackTrace();
            return "admin/add-feedate";
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", "Duplicate entry for "+feedate.getMonthMaster().getMonthName()+". "+de.getLocalizedMessage());
            model.addAttribute("months", monthMasterService.getAllMonths());
            de.printStackTrace();
            return "admin/add-feedate";
        }catch(Exception e){
            model.addAttribute("error", "Error in saving: "+e.getLocalizedMessage());
            model.addAttribute("months", monthMasterService.getAllMonths());
            e.printStackTrace();
            return "admin/add-feedate";
        }
        return "redirect:/admin/feedate";
    }

    // Was @ResponseBody returning JSON for an AJAX call - converted to a plain
    // redirect + flash message so the List page's per-row action can be a real
    // POST <form> (matching universal/section's delete pattern) instead of an
    // AJAX call. That's what closes the CSRF-exempt-GET-style gap the same way
    // Employee/Student/Academic-Year's deletes were fixed - though this one was
    // already POST, so it was already CSRF-safe; this change is about UI
    // consistency, not a new security fix.
    @CheckAccess(screen = "ADMIN_FEEDATE", type = AccessType.DELETE)
    @PostMapping("/feedate/delete/{id}")
    public String deleteFeeDate(@PathVariable("id")Long id, RedirectAttributes redirectAttributes){
        log.info("Inside deleteFeeDate");
        try{
            String returnMsg = feedateService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Fee date deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete fee date.");
            }
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/feedate";
    }

    /*********************************************  Fine Code Block starts here  *****************************************/

    @CheckAccess(screen = "ADMIN_FINE", type = AccessType.VIEW)
    @GetMapping("/fine")
    public String getFineForm(Model model){
        log.info("Inside getFineForm");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<Fine> fineList = fineService.getAllFines(school.getId(), academicYear.getId());
        model.addAttribute("fines", fineList);
        model.addAttribute("isFine", !fineList.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/fine";
    }

    @CheckAccess(screen = "ADMIN_FINE", type = AccessType.CREATE)
    @GetMapping("/fine/add")
    public String getFineAddForm(Model model){
        log.info("Inside getFineAddForm");
        model.addAttribute("fine", new Fine());
        model.addAttribute("fineheads", fineheadService.getAllFineHeads());
        return "admin/add-fine";
    }

    @CheckAccess(screen = "ADMIN_FINE", type = AccessType.CREATE)
    @PostMapping("/fine")
    public String saveFineData(@Valid @ModelAttribute("fine")Fine fine, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside saveFineData");
        if(result.hasErrors()){
            model.addAttribute("fineheads", fineheadService.getAllFineHeads());
            model.addAttribute("error", result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : "Please check the highlighted fields.");
            return "admin/add-fine";
        }
        try{
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            // A tampered hidden "id" on the edit-fine form could point at
            // another school's Fine - without this check, setSchool(school)
            // below would silently reassign (hijack) that other school's
            // record into the caller's own school and overwrite its fields.
            // Confirm the record being updated already belongs to the
            // caller before touching it.
            if (fine.getId() != null && !isSuperAdminLoggedIn()) {
                Fine existingFine = fineService.getFineById(fine.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid fine Id:" + fine.getId()));
                if (school == null || existingFine.getSchool() == null
                        || !existingFine.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Fine.");
                    return "redirect:/admin/fine";
                }
            }
            fine.setAcademicYear(academicYear);
            fine.setSchool(school);
            String returnMsg = "Fine saved successfully for: "+fine.getFinehead().getFineHeadName();
            if(fine.getId()!=null){
                fine.setUpdatedBy(userService.getLoggedInUser());
                returnMsg = "Fine updated successfully for: "+fine.getFinehead().getFineHeadName();
            }
            else{
                fine.setCreatedBy(userService.getLoggedInUser());
            }
            fineService.saveFine(fine);
            redirectAttributes.addFlashAttribute("success",returnMsg);
        }catch(DataIntegrityViolationException de){
            model.addAttribute("error","Duplicate entry for "+fine.getFinehead().getFineHeadName());
            model.addAttribute("fineheads", fineheadService.getAllFineHeads());
            de.printStackTrace();
            return "admin/add-fine";
        }catch(Exception e){
            model.addAttribute("error", "Error in saving: "+e.getLocalizedMessage());
            model.addAttribute("fineheads", fineheadService.getAllFineHeads());
            e.printStackTrace();
            return "admin/add-fine";
        }
        return "redirect:/admin/fine";
    }

    @CheckAccess(screen = "ADMIN_FINE", type = AccessType.EDIT)
    @GetMapping("/fine/edit/{id}")
    public String editFineForm(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editFineForm");
        Fine fine = fineService.getFineById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fine Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || fine.getSchool() == null || !fine.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Fine.");
                return "redirect:/admin/fine";
            }
        }
        model.addAttribute("fine",fine);
        model.addAttribute("fineheads", fineheadService.getAllFineHeads());
        return "admin/edit-fine";
    }

    @CheckAccess(screen = "ADMIN_FINE", type = AccessType.DELETE)
    @PostMapping("/fine/delete/{id}")
    public String deleteFineDate(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteFineDate");
        try{
            if (!isSuperAdminLoggedIn()) {
                Fine existing = fineService.getFineById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Fine not found.");
                    return "redirect:/admin/fine";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Fine.");
                    return "redirect:/admin/fine";
                }
            }
            String returnMsg = fineService.deleteFine(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Fine deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete fine.");
            }
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/fine";
    }

    /****************************  Fee Mapping Code Starts Here  ******************************/

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.VIEW)
    @GetMapping("/fee-class")
    public String getFeeClassDetails(Model model){
        log.info("Inside getFeeClassDetails");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<FeeClassMap> feeClassMaps = feeclassmapService.getAllFeeClassMapping(school.getId(), academicYear.getId());
        model.addAttribute("feeclass", feeClassMaps);
        model.addAttribute("hasFeeClassMap", !feeClassMaps.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/feeclassmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.CREATE)
    @GetMapping("/fee-class/add")
    public String getAddFeeClassMappingForm(Model model){
        log.info("Inside getAddFeeClassMappingForm");
        //model.addAttribute("feeheads", feeheadService.getAllFeeheads());
        model.addAttribute("grades", gradeService.getAllGrades());
        model.addAttribute("mediums", mediumService.getAllMediums());
        FeeClassMapWrapper feeClassMapWrapper = new FeeClassMapWrapper();
        model.addAttribute("feeClassMapWrapper", feeClassMapWrapper);
        return "admin/add-feeclassmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.VIEW)
    @PostMapping("/fee-class/getAllFeeData/{classId}/{mediumId}")
    @ResponseBody
    public Map<String, Map<String, String>> getAllFeeData(@PathVariable("classId")Long classId, @PathVariable("mediumId")Long mediumId, HttpSession session, Model model){
        log.info("Inside getAllFeeData");
        Map<String, Map<String, String>> responseMap = new HashMap<>();
        //map - fee - amount
        try{
            Map<String, String> finalMap = new HashMap<>();
            Set<String> processedFeeheads = new HashSet<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            if (academicYear == null) {
                model.addAttribute("errorMessage", "Academic Year not found in session");
                responseMap.put("error", new HashMap<>()); // Redirect to an error page or display an error message
            }
            List<FeeClassMap> feeClassMapList = feeclassmapService.getAllFeeClassMappingByGrade(classId, mediumId, school.getId(), academicYear.getId());
            List<Feehead> feeheadList = feeheadService.getAllFeeheads();
            if(feeClassMapList!=null && !feeClassMapList.isEmpty()){
                feeClassMapList.forEach(fcm -> {
                    if(feeheadList.contains(fcm.getFeehead())){
                        String feeheadKey = fcm.getFeehead().getId() + ":" + fcm.getFeehead().getFeeHeadName();
                        String finalMapKey = feeheadKey + ":" + fcm.getId();
                        finalMap.put(finalMapKey, fcm.getAmount().toString());
                        processedFeeheads.add(feeheadKey); // Track processed feeheads
                    }
                });
                // Add remaining feeheads that are not present in feeClassMapList
                feeheadList.forEach(fh -> {
                    String feeheadKey = fh.getId() + ":" + fh.getFeeHeadName();
                    if (!processedFeeheads.contains(feeheadKey)) {
                        finalMap.put(feeheadKey + ":-1", "0");
                    }
                });
            } else{
                // If feeClassMapList is empty, add all feeheads with default values
                feeheadList.forEach(fh -> {
                    finalMap.put(fh.getId()+":"+fh.getFeeHeadName()+":-1", "0");
                });
            }
            responseMap.put("success", finalMap);
        }catch(Exception e){
            e.printStackTrace();
            responseMap.put("error", new HashMap<>());
        }
        log.debug("getFeeClassMapData result keys={}", responseMap.keySet());
        return responseMap;
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.CREATE)
    @PostMapping("/fee-class")
    public String saveFeeClassMappings(@ModelAttribute FeeClassMapWrapper feeClassMapWrapper, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside saveFeeClassMappings");
        List<FeeClassMap> feeClassMaps = feeClassMapWrapper.getFeeClassMaps();
        log.debug("saveFeeClassMappings - feeClassMaps size={}", feeClassMaps.size());

        try{
            List<FeeClassMap> feeClassMapList = new ArrayList<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            Grade grade = feeClassMaps.get(0).getGrade();
            Medium medium = feeClassMaps.get(0).getMedium();
            for (FeeClassMap fee : feeClassMaps) {
                // A tampered hidden "id" on one of this matrix's rows could point
                // at another school's existing FeeClassMap - without this check,
                // setSchool()/setGrade()/setMedium() below would silently hijack
                // that row into the caller's own school and overwrite its amount.
                if (fee.getId() != null) {
                    FeeClassMap existingRow = feeclassmapService.getFeeClassMapById(fee.getId()).orElse(null);
                    if (existingRow == null || existingRow.getSchool() == null
                            || !existingRow.getSchool().getId().equals(school.getId())) {
                        redirectAttributes.addFlashAttribute("error", "You do not have access to one of the selected fee heads.");
                        return "redirect:/admin/fee-class";
                    }
                }
                fee.setAcademicYear(academicYear);
                fee.setSchool(school);
                fee.setGrade(grade);
                fee.setMedium(medium);
                fee.setCreatedBy(userService.getLoggedInUser());
                feeClassMapList.add(feeclassmapService.save(fee));
            }
            //Can't use this method because school+academic-year+user details added separately
            //List<FeeClassMap> feeClassMapList = feeclassmapService.saveAllFeeClassMap(feeClassMaps);
            if(feeClassMapList!=null && feeClassMapList.size()>0){
                redirectAttributes.addFlashAttribute("success","Fee-Class Mapping saved for Grade:"+grade.getGradeName());
            } else{
                redirectAttributes.addFlashAttribute("info","Data not saved, re-check the data.");
            }
        }catch(Exception e){
            model.addAttribute("error", "Error: "+e.getLocalizedMessage());
            return "admin/add-feeclassmap";
        }
        return "redirect:/admin/fee-class";
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.EDIT)
    @GetMapping("/fee-class/edit/{id}")
    public String editFeeClassForm(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editFeeClassForm");
        FeeClassMap feeClassMap = feeclassmapService.getFeeClassMapById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fee-class Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || feeClassMap.getSchool() == null
                    || !feeClassMap.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Fee-Grade mapping.");
                return "redirect:/admin/fee-class";
            }
        }
        model.addAttribute("feeclassmap",feeClassMap);
        model.addAttribute("gradename",feeClassMap.getGrade().getGradeName());
        model.addAttribute("mediums", mediumService.getAllMediums());
        return "admin/edit-feeclassmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.EDIT)
    @PostMapping("/edit-fee-class")
    public String updateFeeClassMap(@Valid @ModelAttribute("feeclassmap")FeeClassMap feeClassMap, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside updateFeeClassMap");
        if(result.hasErrors()){
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/edit-feeclassmap";
        }
        try{
            // This form carries hidden id/school_id/academicYear/grade/feehead
            // fields - every one of them just as editable via devtools as any
            // visible input. Never trust them directly: re-fetch the real row
            // by id, confirm it belongs to the caller's school, then apply
            // only the fields the edit form actually lets a user change
            // (Medium/Amount/Description - Grade/Feehead are shown read-only
            // in the UI, so they should never be reassignable through this
            // endpoint either).
            FeeClassMap existing = feeclassmapService.getFeeClassMapById(feeClassMap.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fee-class Id:" + feeClassMap.getId()));
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Fee-Grade mapping.");
                    return "redirect:/admin/fee-class";
                }
            }
            existing.setMedium(feeClassMap.getMedium());
            existing.setAmount(feeClassMap.getAmount());
            existing.setDescription(feeClassMap.getDescription());
            existing.setUpdatedBy(userService.getLoggedInUser());
            feeclassmapService.save(existing);
            ra.addFlashAttribute("info", "Fee-Class mapping updated for Grade: "+existing.getGrade().getGradeName());
        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("error","Error: "+e.getLocalizedMessage());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/edit-feeclassmap";
        }
        return "redirect:/admin/fee-class";
    }

    @CheckAccess(screen = "ADMIN_FEE_CLASS", type = AccessType.DELETE)
    @PostMapping("/fee-class/delete/{id}")
    public String deleteFeeClassMap(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteFeeClassMap");
        try{
            if (!isSuperAdminLoggedIn()) {
                FeeClassMap existing = feeclassmapService.getFeeClassMapById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Fee-Class mapping not found.");
                    return "redirect:/admin/fee-class";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Fee-Class mapping.");
                    return "redirect:/admin/fee-class";
                }
            }
            String returnMsg = feeclassmapService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Fee-Class mapping deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete Fee-Class mapping.");
            }
        }catch(ObjectNotDeleteException oe){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + oe.getLocalizedMessage());
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/fee-class";
    }

    /*****************************  Fee-Month Mapping Code starts here  ********************************/

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.VIEW)
    @GetMapping("/fee-month")
    public String getFeeMonthDetails(Model model){
        log.info("Inside getFeeMonthDetails");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<FeeMonthMap> feeMonthMaps = feemonthmapService.getAllFeeMonthMap(school.getId(), academicYear.getId());
        model.addAttribute("feemonths", feeMonthMaps);
        model.addAttribute("hasFeeMonthMap", !feeMonthMaps.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/feemonthmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.CREATE)
    @GetMapping("/fee-month/add")
    public String getAddFeeMonthMappingForm(Model model){
        log.info("Inside getAddFeeMonthMappingForm");
        model.addAttribute("fees", feeheadService.getAllFeeheads());
        FeeMonthMapWrapper feeMonthMapWrapper = new FeeMonthMapWrapper();
        model.addAttribute("feeMonthMapWrapper", feeMonthMapWrapper);
        return "admin/add-feemonthmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.VIEW)
    @PostMapping("/fee-month/getAllFeeMonthData/{feeId}")
    @ResponseBody
    public Map<String, Map<String, Boolean>> getAllFeeMonthData(@PathVariable("feeId")Long feeId, Model model){
        log.info("Inside getAllFeeMonthData");
        Map<String, Map<String, Boolean>> responseMap = new HashMap<>();
        //map - fee - amount
        try{
            Map<String, Boolean> finalMap = new HashMap<>();
            Set<String> processedMonths = new HashSet<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            List<FeeMonthMap> feeMonthMapList = feemonthmapService.getAllFeeMonthMapByFee(school.getId(), academicYear.getId(), feeId);
            List<MonthMaster> monthMasters = monthMasterService.getAllMonths();
            if(feeMonthMapList!=null && !feeMonthMapList.isEmpty()){
                feeMonthMapList.forEach(fcm -> {
                    if(monthMasters.contains(fcm.getMonthMaster())){
                        String finalMapKey = fcm.getMonthMaster().getId() + ":" + fcm.getMonthMaster().getMonthName() + ":" + fcm.getId();
                        finalMap.put(finalMapKey, fcm.getIsApplicable());
                        processedMonths.add(fcm.getMonthMaster().getId() + ":" + fcm.getMonthMaster().getMonthName()); // Track processed months
                    }
                });
                // Add remaining months that are not present in feeClassMapList
                monthMasters.forEach(fh -> {
                    String feeheadKey = fh.getId() + ":" + fh.getMonthName();
                    if (!processedMonths.contains(feeheadKey)) {
                        finalMap.put(feeheadKey + ":-1", false);
                    }
                });
            } else{
                // If feeMonthMapList is empty, add all months with default values
                monthMasters.forEach(fh -> {
                    finalMap.put(fh.getId()+":"+fh.getMonthName()+":-1", false);
                });
            }
            Map<String, Boolean> sortedSubMap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    // Extract IDs from the keys and compare them
                    int id1 = Integer.parseInt(o1.split(":")[0]);
                    int id2 = Integer.parseInt(o2.split(":")[0]);
                    return Integer.compare(id1, id2);
                }
            });
            sortedSubMap.putAll(finalMap);
            responseMap.put("success", sortedSubMap);
        }catch(Exception e){
            responseMap.put("error", new HashMap<>());
        }
        log.debug("getFeeMonthMapData result keys={}", responseMap.keySet());
        return responseMap;
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.CREATE)
    @PostMapping("/fee-month")
    public String saveFeeMonthMappings(@ModelAttribute FeeMonthMapWrapper feeMonthMapWrapper, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside saveFeeMonthMappings");
        List<FeeMonthMap> feeMonthMaps = feeMonthMapWrapper.getFeeMonthMaps();
        log.debug("saveFeeMonthMappings - feeMonthMaps size={}", feeMonthMaps.size());

        try{
            List<FeeMonthMap> feeMonthMapList = new ArrayList<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            Feehead feehead = feeMonthMaps.get(0).getFeehead();
            for (FeeMonthMap fee : feeMonthMaps) {
                // Same tampered-hidden-id hijack risk as saveFeeClassMappings above.
                if (fee.getId() != null) {
                    FeeMonthMap existingRow = feemonthmapService.getFeeMonthMapById(fee.getId()).orElse(null);
                    if (existingRow == null || existingRow.getSchool() == null
                            || !existingRow.getSchool().getId().equals(school.getId())) {
                        redirectAttributes.addFlashAttribute("error", "You do not have access to one of the selected months.");
                        return "redirect:/admin/fee-month";
                    }
                }
                fee.setAcademicYear(academicYear);
                fee.setSchool(school);
                fee.setFeehead(feehead);
                fee.setCreatedBy(userService.getLoggedInUser());
                feeMonthMapList.add(feemonthmapService.saveFeeMonth(fee));
            }
            //Can't use this method because school+academic-year+user details added separately
            //List<FeeClassMap> feeClassMapList = feeclassmapService.saveAllFeeClassMap(feeClassMaps);
            if(feeMonthMapList!=null && feeMonthMapList.size()>0){
                redirectAttributes.addFlashAttribute("success","Fee-Class Mapping saved for Fee:"+feehead.getFeeHeadName());
            } else{
                redirectAttributes.addFlashAttribute("info","Data not saved, re-check the data.");
            }
        }catch(Exception e){
            model.addAttribute("error", "Error: "+e.getLocalizedMessage());
            return "admin/add-feemonthmap";
        }
        return "redirect:/admin/fee-month";
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.EDIT)
    @GetMapping("/fee-month/edit/{id}")
    public String editFeeMonthForm(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editFeeMonthForm");
        FeeMonthMap feeMonthMap = feemonthmapService.getFeeMonthMapById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fee-month Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || feeMonthMap.getSchool() == null
                    || !feeMonthMap.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Fee-Month mapping.");
                return "redirect:/admin/fee-month";
            }
        }
        model.addAttribute("feemonthmap",feeMonthMap);
        model.addAttribute("monthname",feeMonthMap.getMonthMaster().getMonthName());
        return "admin/edit-feemonthmap";
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.EDIT)
    @PostMapping("/edit-fee-month")
    public String updateFeeMonthMap(@Valid @ModelAttribute("feemonthmap")FeeMonthMap feeMonthMap, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside updateFeeMonthMap");
        if(result.hasErrors()){
            return "admin/edit-feemonthmap";
        }
        try{
            // Same hidden-field tampering risk as Fee-Class mapping: re-fetch
            // the real row by id, confirm ownership, then apply only the
            // fields the edit form actually lets a user change (Applicable/
            // Description - Feehead/Month are shown read-only in the UI).
            FeeMonthMap existing = feemonthmapService.getFeeMonthMapById(feeMonthMap.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fee-month Id:" + feeMonthMap.getId()));
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Fee-Month mapping.");
                    return "redirect:/admin/fee-month";
                }
            }
            existing.setIsApplicable(feeMonthMap.getIsApplicable());
            existing.setDescription(feeMonthMap.getDescription());
            existing.setUpdatedBy(userService.getLoggedInUser());
            feemonthmapService.saveFeeMonth(existing);
            ra.addFlashAttribute("info", "Fee-Month mapping updated for Fee: "+existing.getFeehead().getFeeHeadName());
        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("error","Error: "+e.getLocalizedMessage());
            return "admin/edit-feemonthmap";
        }
        return "redirect:/admin/fee-month";
    }

    @CheckAccess(screen = "ADMIN_FEE_MONTH", type = AccessType.DELETE)
    @PostMapping("/fee-month/delete/{id}")
    public String deleteFeeMonthMap(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteFeeMonthMap");
        try{
            if (!isSuperAdminLoggedIn()) {
                FeeMonthMap existing = feemonthmapService.getFeeMonthMapById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Fee-Month mapping not found.");
                    return "redirect:/admin/fee-month";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Fee-Month mapping.");
                    return "redirect:/admin/fee-month";
                }
            }
            String returnMsg = feemonthmapService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Fee-Month mapping deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete Fee-Month mapping.");
            }
        }catch(ObjectNotDeleteException oe){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + oe.getLocalizedMessage());
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/fee-month";
    }


    /****************************  Discount Mapping Code Starts Here  ******************************/

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.VIEW)
    @GetMapping("/discount-class")
    public String getDiscountClassDetails(Model model){
        log.info("Inside getDiscountClassDetails");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<DiscountClassMap> discountClassMaps = discountclassmapService.getAllDiscountClassMapping(school.getId(), academicYear.getId());
        model.addAttribute("discountclasses", discountClassMaps);
        model.addAttribute("hasDiscountClassMap", !discountClassMaps.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/discountclassmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.CREATE)
    @GetMapping("/discount-class/add")
    public String getAddDiscountClassMappingForm(Model model){
        log.info("Inside getAddDiscountClassMappingForm");
        model.addAttribute("grades", gradeService.getAllGrades());
        model.addAttribute("mediums", mediumService.getAllMediums());
        DiscountClassMapWrapper discountClassMapWrapper = new DiscountClassMapWrapper();
        model.addAttribute("discountClassMapWrapper", discountClassMapWrapper);
        return "admin/add-discountclassmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.VIEW)
    @PostMapping("/discount-class/getAllDiscountData/{classId}/{mediumId}")
    @ResponseBody
    public Map<String, Map<String, String>> getAllDiscountData(@PathVariable("classId")Long classId, @PathVariable("mediumId")Long mediumId, Model model){
        log.info("Inside getAllDiscountData");
        Map<String, Map<String, String>> responseMap = new HashMap<>();
        //map - fee - amount
        try{
            Map<String, String> finalMap = new HashMap<>();
            Set<String> processedDiscountHeads = new HashSet<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            List<DiscountClassMap> discountClassMapList = discountclassmapService.getAllDiscountClassMappingByGrade(school.getId(), academicYear.getId(), classId, mediumId);
            List<Discounthead> discountheadList = discountService.getAllDiscountheads();
            if(discountClassMapList!=null && !discountClassMapList.isEmpty()){
                discountClassMapList.forEach(fcm -> {
                    if(discountheadList.contains(fcm.getDiscounthead())){
                        String feeheadKey = fcm.getDiscounthead().getId() + ":" + fcm.getDiscounthead().getDiscountName();
                        String finalMapKey = feeheadKey + ":" + fcm.getId();
                        finalMap.put(finalMapKey, fcm.getAmount().toString());
                        processedDiscountHeads.add(feeheadKey); // Track processed discountheads
                    }
                });
                // Add remaining discountheads that are not present in discountClassMapList
                discountheadList.forEach(fh -> {
                    String feeheadKey = fh.getId() + ":" + fh.getDiscountName();
                    if (!processedDiscountHeads.contains(feeheadKey)) {
                        finalMap.put(feeheadKey + ":-1", "0");
                    }
                });
            } else{
                // If feeClassMapList is empty, add all feeheads with default values
                discountheadList.forEach(fh -> {
                    finalMap.put(fh.getId()+":"+fh.getDiscountName()+":-1", "0");
                });
            }
            responseMap.put("success", finalMap);
        }catch(Exception e){
            responseMap.put("error", new HashMap<>());
        }
        log.debug("getDiscountClassMapData result keys={}", responseMap.keySet());
        return responseMap;
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.CREATE)
    @PostMapping("/discount-class")
    public String saveDiscountClassMappings(@ModelAttribute DiscountClassMapWrapper discountClassMapWrapper, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside saveDiscountClassMappings");
        List<DiscountClassMap> discountClassMaps = discountClassMapWrapper.getDiscountClassMaps();
        log.debug("saveDiscountClassMappings - discountClassMaps size={}", discountClassMaps.size());

        try{
            List<DiscountClassMap> discountClassMapList = new ArrayList<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            Grade grade = discountClassMaps.get(0).getGrade();
            Medium medium = discountClassMaps.get(0).getMedium();
            for (DiscountClassMap fee : discountClassMaps) {
                // A tampered hidden "id" on one of this matrix's rows could point
                // at another school's existing Discount-Class mapping - without
                // this check, setSchool()/setGrade()/setMedium() below would
                // silently hijack that row into the caller's own school.
                if (fee.getId() != null) {
                    DiscountClassMap existingRow = discountclassmapService.getDiscountClassMapById(fee.getId()).orElse(null);
                    if (existingRow == null || existingRow.getSchool() == null
                            || !existingRow.getSchool().getId().equals(school.getId())) {
                        redirectAttributes.addFlashAttribute("error", "You do not have access to one of the selected discount heads.");
                        return "redirect:/admin/discount-class";
                    }
                }
                fee.setAcademicYear(academicYear);
                fee.setSchool(school);
                fee.setGrade(grade);
                fee.setMedium(medium);
                fee.setCreatedBy(userService.getLoggedInUser());
                discountClassMapList.add(discountclassmapService.save(fee));
            }
            //Can't use this method because school+academic-year+user details added separately
            //List<FeeClassMap> feeClassMapList = feeclassmapService.saveAllFeeClassMap(feeClassMaps);
            if(discountClassMapList!=null && discountClassMapList.size()>0){
                redirectAttributes.addFlashAttribute("success","Discount-Class Mapping saved for Grade:"+grade.getGradeName());
            } else{
                redirectAttributes.addFlashAttribute("info","Data not saved, re-check the data.");
            }
        }catch(Exception e){
            model.addAttribute("error", "Error: "+e.getLocalizedMessage());
            return "admin/add-discountclassmap";
        }
        return "redirect:/admin/discount-class";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.EDIT)
    @GetMapping("/discount-class/edit/{id}")
    public String editDiscountClassForm(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editDiscountClassForm");
        DiscountClassMap discountClassMap = discountclassmapService.getDiscountClassMapById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid discount-class Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || discountClassMap.getSchool() == null
                    || !discountClassMap.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Discount-Grade mapping.");
                return "redirect:/admin/discount-class";
            }
        }
        model.addAttribute("discountclassmap",discountClassMap);
        model.addAttribute("gradename",discountClassMap.getGrade().getGradeName());
        model.addAttribute("mediums", mediumService.getAllMediums());
        return "admin/edit-discountclassmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.EDIT)
    @PostMapping("/edit-discount-class")
    public String updateDiscountClassMap(@Valid @ModelAttribute("discountclassmap")DiscountClassMap discountClassMap, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside updateDiscountClassMap");
        if(result.hasErrors()){
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/edit-discountclassmap";
        }
        try{
            // This form carries hidden id/school_id/academicYear/grade/discounthead
            // fields, all editable via devtools. Re-fetch the real row by id,
            // confirm it belongs to the caller's school, then apply only the
            // fields the edit form actually lets a user change (Medium/Amount/
            // Description - Grade/Discounthead are shown read-only in the UI).
            DiscountClassMap existing = discountclassmapService.getDiscountClassMapById(discountClassMap.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid discount-class Id:" + discountClassMap.getId()));
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Discount-Grade mapping.");
                    return "redirect:/admin/discount-class";
                }
            }
            existing.setMedium(discountClassMap.getMedium());
            existing.setAmount(discountClassMap.getAmount());
            existing.setDescription(discountClassMap.getDescription());
            existing.setUpdatedBy(userService.getLoggedInUser());
            discountclassmapService.save(existing);
            ra.addFlashAttribute("info", "Discount-Class mapping updated for Grade: "+existing.getGrade().getGradeName());
        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("error","Error: "+e.getLocalizedMessage());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/edit-discountclassmap";
        }
        return "redirect:/admin/discount-class";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_CLASS", type = AccessType.DELETE)
    @PostMapping("/discount-class/delete/{id}")
    public String deleteDiscountClassMap(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteDiscountClassMap");
        try{
            if (!isSuperAdminLoggedIn()) {
                DiscountClassMap existing = discountclassmapService.getDiscountClassMapById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Discount-Class mapping not found.");
                    return "redirect:/admin/discount-class";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Discount-Class mapping.");
                    return "redirect:/admin/discount-class";
                }
            }
            String returnMsg = discountclassmapService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Discount-Class mapping deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete Discount-Class mapping.");
            }
        }catch(ObjectNotDeleteException oe){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + oe.getLocalizedMessage());
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/discount-class";
    }

    /*****************************  Discount-Month Mapping Code starts here  ********************************/

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.VIEW)
    @GetMapping("/discount-month")
    public String getDiscountMonthDetails(Model model){
        log.info("Inside getDiscountMonthDetails");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<DiscountMonthMap> discountMonthMaps = discountmonthmapService.getAllDiscountMonthMap(school.getId(), academicYear.getId());
        model.addAttribute("discountmonths", discountMonthMaps);
        model.addAttribute("hasDiscountMonthMap", !discountMonthMaps.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/discountmonthmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.CREATE)
    @GetMapping("/discount-month/add")
    public String getAddDiscountMonthMappingForm(Model model){
        log.info("Inside getAddDiscountMonthMappingForm");
        model.addAttribute("discounts", discountService.getAllDiscountheads());
        DiscountMonthMapWrapper discountMonthMapWrapper = new DiscountMonthMapWrapper();
        model.addAttribute("discountMonthMapWrapper", discountMonthMapWrapper);
        return "admin/add-discountmonthmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.VIEW)
    @PostMapping("/discount-month/getAllDiscountMonthData/{feeId}")
    @ResponseBody
    public Map<String, Map<String, Boolean>> getAllDiscountMonthData(@PathVariable("feeId")Long feeId, Model model){
        log.info("Inside getAllDiscountMonthData");
        Map<String, Map<String, Boolean>> responseMap = new HashMap<>();
        //map - fee - amount
        try{
            Map<String, Boolean> finalMap = new HashMap<>();
            Set<String> processedMonths = new HashSet<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            List<DiscountMonthMap> discountMonthMapList = discountmonthmapService.getAllDiscountMonthMapByDiscount(school.getId(), academicYear.getId(), feeId);
            List<MonthMaster> monthMasters = monthMasterService.getAllMonths();
            if(discountMonthMapList!=null && !discountMonthMapList.isEmpty()){
                discountMonthMapList.forEach(fcm -> {
                    if(monthMasters.contains(fcm.getMonthMaster())){
                        String finalMapKey = fcm.getMonthMaster().getId() + ":" + fcm.getMonthMaster().getMonthName() + ":" + fcm.getId();
                        finalMap.put(finalMapKey, fcm.getIsApplicable());
                        processedMonths.add(fcm.getMonthMaster().getId() + ":" + fcm.getMonthMaster().getMonthName()); // Track processed months
                    }
                });
                // Add remaining months that are not present in feeClassMapList
                monthMasters.forEach(fh -> {
                    String feeheadKey = fh.getId() + ":" + fh.getMonthName();
                    if (!processedMonths.contains(feeheadKey)) {
                        finalMap.put(feeheadKey + ":-1", false);
                    }
                });
            } else{
                // If feeMonthMapList is empty, add all months with default values
                monthMasters.forEach(fh -> {
                    finalMap.put(fh.getId()+":"+fh.getMonthName()+":-1", false);
                });
            }
            Map<String, Boolean> sortedSubMap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    // Extract IDs from the keys and compare them
                    int id1 = Integer.parseInt(o1.split(":")[0]);
                    int id2 = Integer.parseInt(o2.split(":")[0]);
                    return Integer.compare(id1, id2);
                }
            });
            sortedSubMap.putAll(finalMap);
            responseMap.put("success", sortedSubMap);
        }catch(Exception e){
            responseMap.put("error", new HashMap<>());
        }
        log.debug("getDiscountMonthMapData result keys={}", responseMap.keySet());
        return responseMap;
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.CREATE)
    @PostMapping("/discount-month")
    public String saveDiscountMonthMappings(@ModelAttribute DiscountMonthMapWrapper discountMonthMapWrapper, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside saveDiscountMonthMappings");
        List<DiscountMonthMap> discountMonthMaps = discountMonthMapWrapper.getDiscountMonthMaps();
        log.debug("saveDiscountMonthMappings - discountMonthMaps size={}", discountMonthMaps.size());

        try{
            List<DiscountMonthMap> discountMonthMapList = new ArrayList<>();
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            Discounthead feehead = discountMonthMaps.get(0).getDiscounthead();
            for (DiscountMonthMap fee : discountMonthMaps) {
                // Same tampered-hidden-id hijack risk as the Fee-Class/Fee-Month/
                // Discount-Class Add-matrix flows above.
                if (fee.getId() != null) {
                    DiscountMonthMap existingRow = discountmonthmapService.getDiscountMonthMapById(fee.getId()).orElse(null);
                    if (existingRow == null || existingRow.getSchool() == null
                            || !existingRow.getSchool().getId().equals(school.getId())) {
                        redirectAttributes.addFlashAttribute("error", "You do not have access to one of the selected months.");
                        return "redirect:/admin/discount-month";
                    }
                }
                fee.setAcademicYear(academicYear);
                fee.setSchool(school);
                fee.setDiscounthead(feehead);
                fee.setCreatedBy(userService.getLoggedInUser());
                discountMonthMapList.add(discountmonthmapService.saveDiscountMonth(fee));
            }
            //Can't use this method because school+academic-year+user details added separately
            //List<FeeClassMap> feeClassMapList = feeclassmapService.saveAllFeeClassMap(feeClassMaps);
            if(discountMonthMapList!=null && discountMonthMapList.size()>0){
                redirectAttributes.addFlashAttribute("success","Discount-Class Mapping saved for Fee:"+feehead.getDiscountName());
            } else{
                redirectAttributes.addFlashAttribute("info","Data not saved, re-check the data.");
            }
        }catch(Exception e){
            model.addAttribute("error", "Error: "+e.getLocalizedMessage());
            return "admin/add-discountmonthmap";
        }
        return "redirect:/admin/discount-month";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.EDIT)
    @GetMapping("/discount-month/edit/{id}")
    public String editDiscountMonthForm(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editDiscountMonthForm");
        DiscountMonthMap discountMonthMap = discountmonthmapService.getDiscountMonthMapById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid discount-month Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || discountMonthMap.getSchool() == null
                    || !discountMonthMap.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Discount-Month mapping.");
                return "redirect:/admin/discount-month";
            }
        }
        model.addAttribute("discountmonthmap",discountMonthMap);
        model.addAttribute("monthname",discountMonthMap.getMonthMaster().getMonthName());
        return "admin/edit-discountmonthmap";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.EDIT)
    @PostMapping("/edit-discount-month")
    public String updateDiscountMonthMap(@Valid @ModelAttribute("discountmonthmap")DiscountMonthMap discountMonthMap, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside updateDiscountMonthMap");
        if(result.hasErrors()){
            return "admin/edit-discountmonthmap";
        }
        try{
            // Same hidden-field tampering risk as Discount-Class mapping: re-fetch
            // the real row by id, confirm ownership, then apply only the fields
            // the edit form actually lets a user change (Applicable/Description -
            // Discounthead/Month are shown read-only in the UI).
            DiscountMonthMap existing = discountmonthmapService.getDiscountMonthMapById(discountMonthMap.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid discount-month Id:" + discountMonthMap.getId()));
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Discount-Month mapping.");
                    return "redirect:/admin/discount-month";
                }
            }
            existing.setIsApplicable(discountMonthMap.getIsApplicable());
            existing.setDescription(discountMonthMap.getDescription());
            existing.setUpdatedBy(userService.getLoggedInUser());
            discountmonthmapService.saveDiscountMonth(existing);
            ra.addFlashAttribute("info", "Discount-Month mapping updated for Fee: "+existing.getDiscounthead().getDiscountName());
        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("error","Error: "+e.getLocalizedMessage());
            return "admin/edit-discountmonthmap";
        }
        return "redirect:/admin/discount-month";
    }

    @CheckAccess(screen = "ADMIN_DISCOUNT_MONTH", type = AccessType.DELETE)
    @PostMapping("/discount-month/delete/{id}")
    public String deleteDiscountMonthMap(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteDiscountMonthMap");
        try{
            if (!isSuperAdminLoggedIn()) {
                DiscountMonthMap existing = discountmonthmapService.getDiscountMonthMapById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Discount-Month mapping not found.");
                    return "redirect:/admin/discount-month";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Discount-Month mapping.");
                    return "redirect:/admin/discount-month";
                }
            }
            String returnMsg = discountmonthmapService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Discount-Month mapping deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete Discount-Month mapping.");
            }
        }catch(ObjectNotDeleteException oe){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + oe.getLocalizedMessage());
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/discount-month";
    }


    /*****************************  Full payment discount Code starts here  ********************************/

    @CheckAccess(screen = "ADMIN_FULL_PAYMENT", type = AccessType.VIEW)
    @GetMapping("/full-payment-discount")
    public String getFullPaymentDetails(Model model){
        log.info("Inside getFullPaymentDetails");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<FullPayment> fullPaymentList = fullpaymentService.getAllFullPayments(school.getId(), academicYear.getId());
        model.addAttribute("fullpayments", fullPaymentList);
        model.addAttribute("hasFullPayment", !fullPaymentList.isEmpty());
        model.addAttribute("page", "datatable");
        return "admin/fullpayment";
    }

    @CheckAccess(screen = "ADMIN_FULL_PAYMENT", type = AccessType.CREATE)
    @GetMapping("/full-payment-discount/add")
    public String getAddFullPaymentForm(Model model){
        log.info("Inside getAddFullPaymentForm");
        model.addAttribute("grades", gradeService.getAllGrades());
        model.addAttribute("mediums", mediumService.getAllMediums());
        model.addAttribute("fullpayment", new FullPayment());
        return "admin/add-fullpayment";
    }

    @CheckAccess(screen = "ADMIN_FULL_PAYMENT", type = AccessType.CREATE)
    @PostMapping("/full-payment-discount")
    public String saveFullPayment(@Valid @ModelAttribute("fullpayment") FullPayment fullPayment, BindingResult result, Model model, RedirectAttributes ra){
        log.info("Inside saveFullPayment");
        if(result.hasErrors()){
            model.addAttribute("grades", gradeService.getAllGrades());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/add-fullpayment";
        }
        try{
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            // A tampered hidden "id" on the edit form could point at another
            // school's Full-Payment record - without this check, setSchool()
            // below would silently reassign (hijack) that record into the
            // caller's own school and overwrite its fields. Same pattern as
            // saveFineData.
            if (fullPayment.getId() != null && !isSuperAdminLoggedIn()) {
                FullPayment existingFullPayment = fullpaymentService.getFullPaymentById(fullPayment.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid full-payment Id:" + fullPayment.getId()));
                if (school == null || existingFullPayment.getSchool() == null
                        || !existingFullPayment.getSchool().getId().equals(school.getId())) {
                    ra.addFlashAttribute("error", "You do not have access to that Full-Payment record.");
                    return "redirect:/admin/full-payment-discount";
                }
            }
            fullPayment.setAcademicYear(academicYear);
            fullPayment.setSchool(school);
            String returnMsg = "Full-payment saved successfully for: "+fullPayment.getGrade().getGradeName();
            if(fullPayment.getId()!=null){
                returnMsg = "Full-payment updated successfully for: "+fullPayment.getGrade().getGradeName();
            }
            fullpaymentService.save(fullPayment);
            ra.addFlashAttribute("success", returnMsg);
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", de.getLocalizedMessage());
            model.addAttribute("grades", gradeService.getAllGrades());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/add-fullpayment";
        } catch(ObjectNotSaveException oe){
            model.addAttribute("error", oe.getLocalizedMessage());
            model.addAttribute("grades", gradeService.getAllGrades());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/add-fullpayment";
        } catch(Exception e){
            model.addAttribute("error", e.getLocalizedMessage());
            model.addAttribute("grades", gradeService.getAllGrades());
            model.addAttribute("mediums", mediumService.getAllMediums());
            return "admin/add-fullpayment";
        }
        return "redirect:/admin/full-payment-discount";
    }
    @CheckAccess(screen = "ADMIN_FULL_PAYMENT", type = AccessType.EDIT)
    @GetMapping("/full-payment-discount/edit/{id}")
    public String editFullPayment(@PathVariable("id")Long id, Model model, RedirectAttributes ra){
        log.info("Inside editFullPayment");
        FullPayment fullPayment = fullpaymentService.getFullPaymentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid full-payment Id:" + id));
        if (!isSuperAdminLoggedIn()) {
            School school = (School) model.getAttribute("school");
            if (school == null || fullPayment.getSchool() == null
                    || !fullPayment.getSchool().getId().equals(school.getId())) {
                ra.addFlashAttribute("error", "You do not have access to that Full-Payment record.");
                return "redirect:/admin/full-payment-discount";
            }
        }
        model.addAttribute("fullpayment",fullPayment);
        model.addAttribute("mediums", mediumService.getAllMediums());
        return "admin/edit-fullpayment";
    }

    @CheckAccess(screen = "ADMIN_FULL_PAYMENT", type = AccessType.DELETE)
    @PostMapping("/full-payment-discount/delete/{id}")
    public String deleteFullPaymentMap(@PathVariable("id")Long id, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside deleteFullPaymentMap");
        try{
            if (!isSuperAdminLoggedIn()) {
                FullPayment existing = fullpaymentService.getFullPaymentById(id).orElse(null);
                if (existing == null) {
                    redirectAttributes.addFlashAttribute("error", "Full-Payment record not found.");
                    return "redirect:/admin/full-payment-discount";
                }
                School school = (School) model.getAttribute("school");
                if (school == null || existing.getSchool() == null
                        || !existing.getSchool().getId().equals(school.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You do not have access to that Full-Payment record.");
                    return "redirect:/admin/full-payment-discount";
                }
            }
            String returnMsg = fullpaymentService.deleteFullPayment(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Full-Payment record deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete Full-Payment record.");
            }
        }catch(ObjectNotDeleteException oe){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + oe.getLocalizedMessage());
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/full-payment-discount";
    }

    /*************************** User-Role *************************/
    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.VIEW)
    @GetMapping("/user-role-list")
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_ADMIN')")
    public String getUserRoleList(Model model){
        log.info("Inside getUserRoleList");
        List<Employee> employees = null;
        Map<Employee, List<Roles>> userRoleMap = new HashMap<>();
        boolean isSuperAdmin = isSuperAdminLoggedIn();
        boolean isAdmin = isAdminLogin();
        if(isSuperAdmin){
            employees = employeeService.getAllActiveEmployees();
        } else if(isAdmin){
            School school = (School)model.getAttribute("school");
            employees = employeeService.getAllActiveEmployees(school.getId());
        } else{

        }
        if(employees!=null && !employees.isEmpty()){
            for(Employee employee: employees){
                UserEntity user = employee.getUserEntity();
                userRoleMap.put(employee, user.getRoles().isEmpty()?null:user.getRoles());
            }
        }
        model.addAttribute("hasUserRoleMapping",userRoleMap.size()>0?true:false);
        model.addAttribute("isSuperAdminLoggedIn", isSuperAdmin);
        model.addAttribute("isAdminLoggedIn", isAdmin);
        model.addAttribute("userRoleMap", userRoleMap);
        model.addAttribute("isUserRoleMap", !userRoleMap.isEmpty());
        model.addAttribute("employees",employees);
        return "admin/user-role";
    }

    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.CREATE)
    @GetMapping("/add-user-to-role")
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_ADMIN')")
    public String addUserRole(Model model){
        log.info("Inside addUserRole");
        List<Employee> employees = null;
        boolean isSuperAdmin = isSuperAdminLoggedIn();
        boolean isAdmin = isAdminLogin();
        if(isSuperAdmin){
            employees = employeeService.getAllActiveEmployees();
        } else if(isAdmin){
            School school = (School)model.getAttribute("school");
            employees = employeeService.getAllActiveEmployees(school.getId());
        } else{

        }
        /*if(employees!=null && !employees.isEmpty()){
            for(Employee employee: employees){
                UserEntity user = employee.getUserEntity();
                userRoleMap.put(employee, user.getRoles().isEmpty()?null:user.getRoles());
            }
        }*/
        List<Roles> roles = roleRepository.findAll().stream()
                .filter(role -> !role.getName().equals("ROLE_SUPERADMIN")
                             && !role.getName().equals("ROLE_STUDENT"))
                .collect(Collectors.toList());
        model.addAttribute("employees", employees);
        model.addAttribute("hasEmployee", !employees.isEmpty());
        model.addAttribute("roles",roles);
        model.addAttribute("hasRoles",!roles.isEmpty());
        return "admin/add-user-role-map";
    }

    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.VIEW)
    @GetMapping("/api/user-role/existing-roles/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_ADMIN')")
    public ResponseEntity<?> getExistingRoles(@PathVariable("employeeId") Long employeeId, Model model) {
        log.info("Inside getExistingRoles");
        try {
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || !employeeService.employeeBelongsToSchool(employeeId, school.getId())) {
                    return ResponseEntity.status(403).body("You do not have access to this employee.");
                }
            }
            List<String> roleNames = employeeService.getExistingRoleNames(employeeId);
            return ResponseEntity.ok(roleNames);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching roles: " + e.getMessage());
        }
    }

    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.CREATE)
    @PostMapping("/api/user-role/save")
    public ResponseEntity<?> saveRoleUserMapping(@RequestBody Map<String, Long> payload, Model model){
        log.info("Inside saveRoleUserMapping");
        try {
            log.debug("saveRoleUserMapping payload={}", payload);
            if(payload!=null){
                Long employeeId = payload.get("employeeId");
                Long roleId = payload.get("roleId");
                if (!isSuperAdminLoggedIn()) {
                    School school = (School) model.getAttribute("school");
                    if (school == null || !employeeService.employeeBelongsToSchool(employeeId, school.getId())) {
                        return ResponseEntity.status(403).body("You do not have access to this employee.");
                    }
                }
                boolean b = employeeService.saveRoleUserMapping(employeeId, roleId);
                if(!b){
                    return ResponseEntity.ok("Either unable to assign the Role to User or Role already assigned");
                } else{
                    return ResponseEntity.ok("Role assigned successfully");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error assigning role: " + e.getMessage());
        }
        return ResponseEntity.status(400).body("Unexpected error occurred");
    }

    // Roles currently assigned to an employee, WITH role IDs - feeds the Manage
    // Roles / Revoke modal. getExistingRoles (above) only returns display
    // strings, no ID, so it can't be used to build a revoke call.
    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.VIEW)
    @GetMapping("/api/user-role/existing-roles-detailed/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_ADMIN')")
    public ResponseEntity<?> getExistingRolesDetailed(@PathVariable("employeeId") Long employeeId, Model model) {
        log.info("Inside getExistingRolesDetailed");
        try {
            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || !employeeService.employeeBelongsToSchool(employeeId, school.getId())) {
                    return ResponseEntity.status(403).body("You do not have access to this employee.");
                }
            }
            return ResponseEntity.ok(employeeService.getExistingRolesDetailed(employeeId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching roles: " + e.getMessage());
        }
    }

    // Revoke a role from a user. Restricted to ROLE_SUPERADMIN/ROLE_ADMIN - same
    // access level the whole Role-User Mapping page already requires. A user can
    // never revoke their OWN Super Admin (ROLE_SUPERADMIN/ROLE_ADMIN) role here -
    // that guard is enforced below regardless of who's logged in, to prevent an
    // accidental self-lockout with nobody left to undo it.
    @CheckAccess(screen = "ADMIN_USERROLE", type = AccessType.DELETE)
    @PostMapping("/api/user-role/revoke")
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_ADMIN')")
    public ResponseEntity<?> revokeRoleUserMapping(@RequestBody Map<String, Long> payload, Model model){
        log.info("Inside revokeRoleUserMapping");
        try {
            log.debug("revokeRoleUserMapping payload={}", payload);
            if(payload == null){
                return ResponseEntity.status(400).body("Unexpected error occurred");
            }
            Long employeeId = payload.get("employeeId");
            Long roleId = payload.get("roleId");
            if(employeeId == null || roleId == null){
                return ResponseEntity.status(400).body("employeeId and roleId are required");
            }

            if (!isSuperAdminLoggedIn()) {
                School school = (School) model.getAttribute("school");
                if (school == null || !employeeService.employeeBelongsToSchool(employeeId, school.getId())) {
                    return ResponseEntity.status(403).body("You do not have access to this employee.");
                }
            }

            Roles role = roleRepository.findById(roleId).orElse(null);
            boolean isSuperAdminRole = role != null &&
                    ("ROLE_SUPERADMIN".equals(role.getName()) || "ROLE_ADMIN".equals(role.getName()));
            if(isSuperAdminRole){
                Long targetUserId = employeeService.getUserIdForEmployee(employeeId);
                Long loggedInUserId = userService.getLoggedInUser() != null ? userService.getLoggedInUser().getId() : null;
                if(targetUserId != null && targetUserId.equals(loggedInUserId)){
                    return ResponseEntity.ok("You cannot revoke your own Super Admin role.");
                }
            }
            boolean removed = employeeService.removeRoleFromUser(employeeId, roleId);
            if(!removed){
                return ResponseEntity.ok("Either unable to revoke the role or it is not currently assigned");
            } else{
                return ResponseEntity.ok("Role revoked successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error revoking role: " + e.getMessage());
        }
    }

    private boolean isSuperAdminLoggedIn(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // Get logged-in username
            if(authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean isAdminLogin(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // Check if the user has the "ROLE_ADMIN"
                return authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            }
            return false;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /********************************   Holiday Code starts here   ************************************/

    @CheckAccess(screen = "ADMIN_HOLIDAY", type = AccessType.VIEW)
    @GetMapping("/holidays")
    public String getHoliday(Model model){
        log.info("Inside getHoliday");
        //Get data of school and academicyear when loggedin
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<Holiday> holidayList = holidayService.getAllHoliday(academicYear.getId(), school.getId());
        model.addAttribute("holidays", holidayList);
        model.addAttribute("isHoliDays", !holidayList.isEmpty());
        // Needed for the inline "quick add" panel's th:object="${holiday}" binding.
        model.addAttribute("holiday", new Holiday());
        return "admin/holiday";
    }

    @CheckAccess(screen = "ADMIN_HOLIDAY", type = AccessType.CREATE)
    @GetMapping("/holiday/add")
    public String getAddHolidayForm(Model model){
        log.info("Inside getAddHolidayForm");
        model.addAttribute("holiday", new Holiday());
        return "admin/add-holiday";
    }

    @CheckAccess(screen = "ADMIN_HOLIDAY", type = AccessType.CREATE)
    @PostMapping("/holiday")
    public String save(@Valid @ModelAttribute("holiday")Holiday holiday, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside save");
        if(result.hasErrors()){
            // Was the raw FieldError object - see the identical comment on
            // Fee Date's save() for why this is now a plain message string.
            model.addAttribute("error", result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : "Please check the highlighted fields.");
            return "admin/add-holiday";
        }
        try{
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            holiday.setAcademicYear(academicYear);
            holiday.setSchool(school);
            holiday = holidayService.save(holiday);
            log.info("Holiday saved: id={}", holiday.getId());
            redirectAttributes.addFlashAttribute("success","Holiday saved successfully for: "+holiday.getHolidayName());
        }catch(DataIntegrityViolationException de){
            model.addAttribute("error", "Duplicate entry for "+holiday.getHolidayName());
            log.error("Duplicate holiday entry for {}", holiday.getHolidayName(), de);
            return "admin/add-holiday";
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", "Duplicate entry for "+holiday.getHolidayName()+". "+de.getLocalizedMessage());
            log.error("Duplicate holiday entry for {}", holiday.getHolidayName(), de);
            return "admin/add-holiday";
        }catch(Exception e){
            model.addAttribute("error", "Error in saving: "+e.getLocalizedMessage());
            log.error("Error saving holiday", e);
            return "admin/add-holiday";
        }
        return "redirect:/admin/holidays";
    }
    // Converted from @ResponseBody JSON to a plain redirect + flash message -
    // see the identical comment on Fee Date's deleteFeeDate() above.
    @CheckAccess(screen = "ADMIN_HOLIDAY", type = AccessType.DELETE)
    @PostMapping("/holiday/delete/{id}")
    public String deleteHoliday(@PathVariable("id")Long id, RedirectAttributes redirectAttributes){
        log.info("Inside deleteHoliday");
        try{
            String returnMsg = holidayService.delete(id);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Holiday deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete holiday.");
            }
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/holidays";
    }

    /******************************* Examination Code Starts Here *******************************/
    @CheckAccess(screen = "ADMIN_EXAM", type = AccessType.VIEW)
    @GetMapping("/examinations")
    public String getExaminations(Model model){
        log.info("Inside getExaminations");
        List<Examination> examinationList = examinationService.getAllExamination();
        model.addAttribute("examinations", examinationList);
        model.addAttribute("isExamination", !examinationList.isEmpty());
        // Needed for the inline "quick add" panel's th:object="${examination}" binding.
        model.addAttribute("examination", new Examination());
        return "admin/examination";
    }

    @CheckAccess(screen = "ADMIN_EXAM", type = AccessType.CREATE)
    @GetMapping("/examination/add")
    public String getAddExaminationForm(Model model){
        log.info("Inside getAddExaminationForm");
        model.addAttribute("examination", new Examination());
        return "admin/add-examination";
    }

    // Converted from @ResponseBody JSON to a plain redirect + flash message -
    // see the identical comment on Fee Date's deleteFeeDate() above.
    @CheckAccess(screen = "ADMIN_EXAM", type = AccessType.DELETE)
    @PostMapping("/examination/delete/{id}")
    public String deleteExamination(@PathVariable("id")String uuid, RedirectAttributes redirectAttributes){
        log.info("Inside deleteExamination");
        try{
            String returnMsg = examinationService.deleteExamination(uuid);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Examination deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete examination.");
            }
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/examinations";
    }

    @CheckAccess(screen = "ADMIN_EXAM", type = AccessType.CREATE)
    @PostMapping("/examination")
    public String save(@Valid @ModelAttribute("examination")Examination examination, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside save");
        if(result.hasErrors()){
            // Was the raw FieldError object - see the identical comment on
            // Fee Date's save() for why this is now a plain message string.
            model.addAttribute("error", result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : "Please check the highlighted fields.");
            return "admin/add-examination";
        }
        try{
            examination = examinationService.save(examination);
            log.info("Examination saved: id={}", examination.getId());
            redirectAttributes.addFlashAttribute("success","Examination: "+examination.getExaminationName()+" saved successfully.");
        }catch(DataIntegrityViolationException de){
            model.addAttribute("error", "Duplicate entry for "+examination.getExaminationName());
            log.error("Duplicate examination entry for {}", examination.getExaminationName(), de);
            return "admin/add-examination";
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", "Duplicate entry for "+examination.getExaminationName()+". "+de.getLocalizedMessage());
            log.error("Duplicate examination entry for {}", examination.getExaminationName(), de);
            return "admin/add-examination";
        }catch(Exception e){
            model.addAttribute("error", "Error in saving: "+e.getLocalizedMessage());
            log.error("Error saving examination", e);
            return "admin/add-examination";
        }
        return "redirect:/admin/examinations";
    }

    @CheckAccess(screen = "ADMIN_EXAM_DATE", type = AccessType.VIEW)
    @GetMapping("/examinations-date")
    public String getExaminationsDate(Model model){
        log.info("Inside getExaminationsDate");
        School school = (School)model.getAttribute("school");
        AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
        List<ExamDetails> examinationList = examinationService.getAllExaminationDates(academicYear.getId(), school.getId());
        model.addAttribute("examinations", examinationList);
        model.addAttribute("isExamination", !examinationList.isEmpty());
        // Needed for the inline "quick add" panel's th:object="${examDetails}"
        // binding and its Examination dropdown - note this "examinations"
        // model key is intentionally overwritten below: the list rows above use
        // the ExamDetails list under the same name, but the inline panel's
        // <select> needs the Examination master list instead, and the panel
        // renders after the table in the page, so the later value wins.
        model.addAttribute("examDetails", new ExamDetails());
        model.addAttribute("examinationOptions", examinationService.getAllExamination());
        return "admin/examination_date";
    }

    @CheckAccess(screen = "ADMIN_EXAM_DATE", type = AccessType.CREATE)
    @GetMapping("/examination-details/add")
    public String getAddExaminationDateForm(Model model){
        log.info("Inside getAddExaminationDateForm");
        model.addAttribute("examDetails", new ExamDetails());
        model.addAttribute("examinations", examinationService.getAllExamination());
        return "admin/add-examination-details";
    }

    @CheckAccess(screen = "ADMIN_EXAM_DATE", type = AccessType.CREATE)
    @PostMapping("/examination-details")
    public String save(@Valid @ModelAttribute("examDetails")ExamDetails examDetails, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        log.info("Inside save");
        model.addAttribute("examinations", examinationService.getAllExamination());
        if(result.hasErrors()){
            // Was the raw FieldError object - see the identical comment on
            // Fee Date's save() for why this is now a plain message string.
            model.addAttribute("error", result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : "Please check the highlighted fields.");
            //model.addAttribute("examinations", examinationService.getAllExamination());
            return "admin/add-examination-details";
        }
        try{
            School school = (School)model.getAttribute("school");
            AcademicYear academicYear = (AcademicYear)model.getAttribute("academicYear");
            examDetails.setAcademicYear(academicYear);
            examDetails.setSchool(school);
            examDetails = examinationService.saveExamDetails(examDetails);
            log.info("Examination details saved: id={}", examDetails.getId());
            SimpleDateFormat sf = new SimpleDateFormat("dd/MMM/yyyy");
            redirectAttributes.addFlashAttribute("success","Examination: "+examDetails.getExamination().getExaminationName()+" scheduled on: "+sf.format(examDetails.getExamDeclaredDate())+" successfully.");
        }catch(DataIntegrityViolationException de){
            model.addAttribute("error", "Duplicate entry for "+examDetails.getExamination().getExaminationName());
            log.error("Duplicate examination details entry for {}", examDetails.getExamination().getExaminationName(), de);
            return "admin/add-examination-details";
        }catch(UniqueConstraintsException de){
            model.addAttribute("error", "Duplicate entry for "+examDetails.getExamination().getExaminationName()+". "+de.getLocalizedMessage());
            log.error("Duplicate examination details entry for {}", examDetails.getExamination().getExaminationName(), de);
            return "admin/add-examination-details";
        }catch(Exception e){
            model.addAttribute("error", "Error in saving: "+e.getLocalizedMessage());
            log.error("Error saving examination details", e);
            return "admin/add-examination-details";
        }
        return "redirect:/admin/examinations-date";
    }

    // Converted from @ResponseBody JSON to a plain redirect + flash message -
    // see the identical comment on Fee Date's deleteFeeDate() above.
    @CheckAccess(screen = "ADMIN_EXAM_DATE", type = AccessType.DELETE)
    @PostMapping("/examinations-detail/delete/{id}")
    public String deleteExaminationDetail(@PathVariable("id")String uuid, RedirectAttributes redirectAttributes){
        log.info("Inside deleteExaminationDetail");
        try{
            String returnMsg = examinationService.deleteExamDetails(uuid);
            if ("success".equals(returnMsg)) {
                redirectAttributes.addFlashAttribute("success", "Examination detail deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete examination detail.");
            }
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", "Error in deletion: " + e.getLocalizedMessage());
        }
        return "redirect:/admin/examinations-date";
    }

}
