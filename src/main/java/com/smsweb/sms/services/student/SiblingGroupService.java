package com.smsweb.sms.services.student;


import com.smsweb.sms.models.admin.AcademicYear;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.student.AcademicStudent;
import com.smsweb.sms.models.student.SiblingGroup;
import com.smsweb.sms.models.student.SiblingGroupStudent;
import com.smsweb.sms.repositories.admin.AcademicyearRepository;
import com.smsweb.sms.repositories.admin.SchoolRepository;
import com.smsweb.sms.repositories.student.AcademicStudentRepository;
import com.smsweb.sms.repositories.student.SiblingGroupRepository;
import com.smsweb.sms.repositories.student.SiblingGroupStudentRepository;
import com.smsweb.sms.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class SiblingGroupService {
    private static final Logger log = LoggerFactory.getLogger(SiblingGroupService.class);

    private final UserService userService;
    private SiblingGroupRepository siblingGroupRepository;
    private final AcademicyearRepository academicyearRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicStudentRepository academicStudentRepository;
    private final SiblingGroupStudentRepository siblingGroupStudentRepository;

    @Autowired
    public SiblingGroupService(SiblingGroupRepository siblingGroupRepository, AcademicyearRepository academicyearRepository, SchoolRepository schoolRepository, AcademicStudentRepository academicStudentRepository, SiblingGroupStudentRepository siblingGroupStudentRepository, UserService userService){
        this.siblingGroupRepository = siblingGroupRepository;
        this.academicyearRepository = academicyearRepository;
        this.schoolRepository = schoolRepository;
        this.academicStudentRepository = academicStudentRepository;
        this.siblingGroupStudentRepository = siblingGroupStudentRepository;
        this.userService = userService;
    }

    public List<SiblingGroup> getAllSiblingGroups(Long school_id, Long academic_year_id){
        log.info("Inside getAllSiblingGroups");
        return siblingGroupRepository.findAllBySchool_IdAndAcademicYear_IdAndStatus(school_id, academic_year_id, "Active");
    }

    @Transactional
    public Map save(Map<String, String[]> paramsMap, School school, AcademicYear academicYear){
        log.info("Inside save");
        Map resultMap = new HashMap();
        try{
            if(paramsMap!=null && !paramsMap.isEmpty()){
                log.debug("save siblingGroup - paramsMap keys={}", paramsMap.keySet());
                List<SiblingGroup> siblingGroupList = new ArrayList<>();
                List<AcademicStudent> academicStudentList = new ArrayList<>();
                String groupName = "";
                for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    for (String value : values) {
                        if(key.equalsIgnoreCase("groupname")){
                            groupName = value;
                        } else if(key.equalsIgnoreCase("academicstudent")){
                            // Deliberately NOT restricted to the current school/year:
                            // Sibling Group is an intentionally cross-branch feature -
                            // "Add Manually" searches ALL active students across every
                            // branch (see FeeSubmissionRestController#searchStudentForSiblingPage),
                            // so a family with children at two different branches can
                            // still be grouped together here. Only requires the id to
                            // resolve to a real AcademicStudent at all.
                            AcademicStudent academicStudent = academicStudentRepository.findById(Long.parseLong(value)).orElse(null);
                            if (academicStudent == null) {
                                throw new RuntimeException("One of the selected students could not be found.");
                            }
                            academicStudentList.add(academicStudent);
                        }
                    }
                }
                int counter = 0;
                // Cross-school duplicate guard: block if any student is already in an active sibling group
                String proceedMsg = validateExistingGroupStudent(academicStudentList, school, academicYear);
                log.debug("validateExistingGroupStudent result: {}", proceedMsg);
                if (proceedMsg != null && !proceedMsg.trim().isEmpty()) {
                    if (proceedMsg.startsWith("Found")) {
                        // Extract the human-readable message after the prefix
                        String detail = proceedMsg.substring("Found:".length());
                        resultMap.put("STUDENT_EXIST", detail);
                        log.warn("Sibling group save blocked — {}", detail);
                        return resultMap;
                    }
                    // Any other non-empty message (Error:...) → also block to be safe
                    if (proceedMsg.startsWith("Error")) {
                        resultMap.put("STUDENT_EXIST", "Validation error: " + proceedMsg.substring("Error:".length()));
                        return resultMap;
                    }
                }
                if(academicStudentList!=null && !academicStudentList.isEmpty()){
                    SiblingGroup siblingGroup = new SiblingGroup();
                    siblingGroup.setGroupName(groupName);
                    siblingGroup.setAcademicYear(academicYear);
                    siblingGroup.setSchool(school);
                    siblingGroup.setDescription("Saving group with student(s)");
                    siblingGroup.setCreatedBy(userService.getLoggedInUser());
                    SiblingGroup group = siblingGroupRepository.save(siblingGroup);
                    siblingGroupList.add(group);
                    for(AcademicStudent student: academicStudentList){
                        SiblingGroupStudent groupStudent = new SiblingGroupStudent();
                        groupStudent.setAcademicStudent(student);
                        groupStudent.setSiblingGroup(group);
                        siblingGroupStudentRepository.save(groupStudent);
                        counter++;
                    }
                }
                if(academicStudentList.size()==counter){
                    resultMap.put("SAVED","1");
                } else{
                    resultMap.put("NOT_SAVED","1");
                }

                resultMap.put("siblingGroup", siblingGroupList);
            }
        }catch(Exception e){
            e.printStackTrace();
            resultMap.put("siblingGroup", null);
            resultMap.put("error", e.getLocalizedMessage());
        }
        return resultMap;
    }

    /**
     * Validates that none of the provided students are already in an active sibling group
     * ACROSS ALL BRANCHES (cross-school check using student.id, not academicStudent.id).
     *
     * This prevents the scenario where Branch A creates a group for siblings A+B+C,
     * and Branch B staff creates a duplicate group for the same physical students.
     *
     * Called at save time as the hard enforcement layer.
     */
    public String validateExistingGroupStudent(List<AcademicStudent> academicStudents, School school, AcademicYear academicYear) {
        log.info("Inside validateExistingGroupStudent (cross-school check)");
        if (academicStudents == null || academicStudents.isEmpty()) {
            return "Error:No academic student found in list.";
        }
        try {
            for (AcademicStudent as : academicStudents) {
                Long studentId = as.getStudent().getId();
                List<SiblingGroupStudent> existing = siblingGroupStudentRepository.findActiveGroupsByStudentId(studentId);
                if (existing != null && !existing.isEmpty()) {
                    SiblingGroup g = existing.get(0).getSiblingGroup();
                    return "Found:Student '" + as.getStudent().getStudentName() +
                           "' is already in sibling group '" + g.getGroupName() +
                           "' at " + g.getSchool().getSchoolName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error:" + e.getLocalizedMessage();
        }
        return "";
    }

    /**
     * UX-level eligibility check — called from the REST endpoint when staff clicks "Add Student".
     * Returns a map with either {eligible: true} or {blocked: true, groupName, schoolName}.
     */
    public Map<String, Object> checkSiblingEligibility(Long studentId) {
        log.info("Inside checkSiblingEligibility - studentId={}", studentId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<SiblingGroupStudent> existing = siblingGroupStudentRepository.findActiveGroupsByStudentId(studentId);
            if (existing != null && !existing.isEmpty()) {
                SiblingGroup g = existing.get(0).getSiblingGroup();
                result.put("blocked", true);
                result.put("groupName", g.getGroupName());
                result.put("schoolName", g.getSchool().getSchoolName());
            } else {
                result.put("eligible", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getLocalizedMessage());
        }
        return result;
    }


    public Optional<SiblingGroup> getSiblingGroupDetail(Long id, Long schoolId){
        // School-scoped - without this, any admin could view another school's
        // sibling group (and the students in it) just by knowing/guessing its id.
        Optional<SiblingGroup> group = siblingGroupRepository.findById(id);
        if (group.isPresent() && (group.get().getSchool() == null
                || schoolId == null || !schoolId.equals(group.get().getSchool().getId()))) {
            return Optional.empty();
        }
        return group;
    }

    @Transactional
    public String deleteSiblingGroup(Long id, Long schoolId){
        log.info("Inside deleteSiblingGroup");
        String msg = "";
        try{
            SiblingGroup siblingGroup = siblingGroupRepository.findById(id).orElse(null);
            // School-scoped - without this, any admin could delete another
            // school's sibling group just by knowing/guessing its id.
            if (siblingGroup == null || siblingGroup.getSchool() == null
                    || schoolId == null || !schoolId.equals(siblingGroup.getSchool().getId())) {
                return "Error: Sibling group not found in your current school.";
            }
            siblingGroupRepository.delete(siblingGroup);

            //siblingGroupRepository.deleteById(id);
            msg = "success";
        }catch(Exception e){
            e.printStackTrace();
            msg = "Error: "+e.getLocalizedMessage();
        }
        return msg;
    }

    public List<SiblingGroupStudent> getAllStudentByGroup(Long siblingId){
        log.info("Inside getAllStudentByGroup - siblingId={}", siblingId);
        try{
            SiblingGroup siblingGroup = siblingGroupRepository.findById(siblingId).orElse(null);
            log.debug("siblingGroup found={}", siblingGroup != null);
            if(siblingGroup!=null){
                List<SiblingGroupStudent> lst = siblingGroup.getSiblingGroupStudents();
                return lst;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
