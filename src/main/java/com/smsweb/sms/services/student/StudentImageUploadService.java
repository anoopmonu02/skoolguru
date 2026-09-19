package com.smsweb.sms.services.student;

import com.smsweb.sms.helper.FileHandleHelper;
import com.smsweb.sms.models.student.AcademicStudent;
import com.smsweb.sms.models.student.Student;
import com.smsweb.sms.repositories.student.AcademicStudentRepository;
import com.smsweb.sms.repositories.student.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * NEW, isolated service backing "Update Student Images (Group-wise)". Does
 * not modify StudentService, StudentBulkUpdateService, or any other existing
 * service - reads/writes Student.pic only, via the same repositories those
 * classes already use.
 *
 * Every write is re-scoped to the CURRENT school/academic year, the same
 * IDOR-safe pattern StudentBulkUpdateService.saveFieldGroup() uses: the
 * uuid -> AcademicStudent lookup is rejected unless it matches the caller's
 * own school + academic year, taken from the server-side session - never
 * from client input.
 */
@Service
public class StudentImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(StudentImageUploadService.class);

    @Autowired
    private AcademicStudentRepository academicStudentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FileHandleHelper fileHandleHelper;

    public List<Map<String, Object>> getRowsForImageUpload(Long mediumId, Long gradeId, Long sectionId,
                                                             Long academicYearId, Long schoolId) {
        log.info("Inside getRowsForImageUpload");
        List<AcademicStudent> list = academicStudentRepository
                .findAllBySchool_IdAndMedium_IdAndGrade_IdAndSection_IdAndAcademicYear_IdAndStatusIgnoreCase(
                        schoolId, mediumId, gradeId, sectionId, academicYearId, "Active");

        List<Map<String, Object>> rows = new ArrayList<>();
        int sno = 1;
        for (AcademicStudent as : list) {
            Student s = as.getStudent();
            if (s == null || as.getUuid() == null) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sno", sno++);
            row.put("uuid", as.getUuid().toString());
            row.put("studentName", s.getStudentName() != null ? s.getStudentName() : "");
            row.put("fatherName", s.getFatherName() != null ? s.getFatherName() : "");
            row.put("motherName", s.getMotherName() != null ? s.getMotherName() : "");
            row.put("psrn", s.getPsrn() != null ? s.getPsrn() : "");
            row.put("pic", s.getPic());
            rows.add(row);
        }
        return rows;
    }

    /**
     * Saves one row's photo. Row-wise, not part of the page-wide bulk-save
     * flow the "Update Student Details" page uses - each row's Save button
     * calls this independently. On success, deletes whatever photo the
     * student had before (the storage-leak fix: previously nothing on this
     * app's web side ever deleted the old file on re-upload) - only after
     * the new file is already saved and Student.pic already points at it,
     * so a student is never left with no usable photo if anything above
     * this point failed.
     */
    @Transactional
    public Map<String, Object> saveStudentImage(String uuid, MultipartFile file, Long academicYearId, Long schoolId) {
        log.info("Inside saveStudentImage, uuid={}", uuid);
        if (file == null || file.isEmpty()) {
            return result(false, "Please choose a photo to upload.", null);
        }

        AcademicStudent as;
        try {
            as = academicStudentRepository.findByUuid(UUID.fromString(uuid)).orElse(null);
        } catch (IllegalArgumentException e) {
            return result(false, "Missing student reference.", null);
        }
        // Same IDOR-safe re-check as StudentBulkUpdateService.saveFieldGroup(): stops a
        // tampered uuid from reading/writing a student outside the caller's own school.
        // schoolId/academicYearId themselves always come from the server-side session
        // (BaseController), never from the request.
        if (as == null || as.getStudent() == null
                || as.getSchool() == null || !as.getSchool().getId().equals(schoolId)
                || as.getAcademicYear() == null || !as.getAcademicYear().getId().equals(academicYearId)) {
            return result(false, "Student not found for the current school/academic year.", null);
        }

        String imageResponse;
        try {
            // Existing 2MB size cap and content-type check, unchanged - no new
            // validation logic added here, per the explicit "keep 2MB, no new
            // logic" decision. Same helper add-student/edit-student already use.
            imageResponse = fileHandleHelper.saveImage("student", file);
        } catch (IOException e) {
            log.error("Error saving student image for uuid={}", uuid, e);
            return result(false, "Failed to save the image: " + e.getMessage(), null);
        }
        if (isFailureResponse(imageResponse)) {
            return result(false, imageResponse == null || imageResponse.isBlank()
                    ? "Failed to save the image." : imageResponse, null);
        }

        Student student = as.getStudent();
        String previousPic = student.getPic();
        student.setPic(imageResponse);
        studentRepository.save(student);

        if (previousPic != null && !previousPic.isBlank() && !previousPic.equals(imageResponse)) {
            fileHandleHelper.deleteStudentImage(previousPic);
        }

        return result(true, "Photo saved", imageResponse);
    }

    /**
     * FileHandleHelper.saveImage() signals failure by returning one of a few
     * known human-readable strings instead of the saved filename - same
     * convention StudentService.saveStudent()/editStudentDetails() already
     * check for when calling the same method.
     */
    private boolean isFailureResponse(String imageResponse) {
        if (imageResponse == null || imageResponse.isBlank()) return true;
        return imageResponse.equalsIgnoreCase("Success_no_image")
                || imageResponse.equalsIgnoreCase("Either image format not supported or size exceeded 2MB.")
                || imageResponse.startsWith("Failed to save the image: ")
                || imageResponse.equalsIgnoreCase("Specified category not valid");
    }

    private Map<String, Object> result(boolean success, String message, String fileName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", success);
        map.put("message", message);
        if (fileName != null) map.put("fileName", fileName);
        return map;
    }
}
