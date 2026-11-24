package com.smsweb.sms.services.smsmessage;

import com.smsweb.sms.dto.ClassRecipientCount;
import com.smsweb.sms.helper.FileHandleHelper;
import com.smsweb.sms.models.Users.UserEntity;
import com.smsweb.sms.models.admin.School;
import com.smsweb.sms.models.messaging.SmsConversation;
import com.smsweb.sms.models.messaging.SmsMessage;
import com.smsweb.sms.models.messaging.SmsMessageAttachment;
import com.smsweb.sms.models.universal.Grade;
import com.smsweb.sms.models.universal.Medium;
import com.smsweb.sms.models.universal.Section;
import com.smsweb.sms.repositories.student.AcademicStudentRepository;
import com.smsweb.sms.services.globalaccess.DropdownService;
import com.smsweb.sms.services.universal.GradeService;
import com.smsweb.sms.services.universal.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Notification to SEVERAL classes at once (Send Message > Notification > Class), with an optional
 * Medium filter. Every selected grade is paired with every selected section; each pair that has
 * Active students gets ONE message that behaves exactly like today's single-class send.
 *
 * Deliberately NOT @Transactional: each class is saved in its own transaction (same as the existing
 * send), so one failing class can never undo the others. Preview and send use the same counting
 * code. School always comes from the logged-in employee; every id is checked against the real lists.
 */
@Service
public class ClassNotificationService {
    private static final Logger log = LoggerFactory.getLogger(ClassNotificationService.class);

    public static final int MAX_SELECTION = 60;

    private final AcademicStudentRepository academicStudentRepository;
    private final SmsMessageService smsMessageService;
    private final GradeService gradeService;
    private final SectionService sectionService;
    private final DropdownService dropdownService;
    private final FileHandleHelper fileHandleHelper;

    public ClassNotificationService(AcademicStudentRepository academicStudentRepository,
                                    SmsMessageService smsMessageService,
                                    GradeService gradeService,
                                    SectionService sectionService,
                                    DropdownService dropdownService,
                                    FileHandleHelper fileHandleHelper) {
        this.academicStudentRepository = academicStudentRepository;
        this.smsMessageService = smsMessageService;
        this.gradeService = gradeService;
        this.sectionService = sectionService;
        this.dropdownService = dropdownService;
        this.fileHandleHelper = fileHandleHelper;
    }

    /** Thrown for a bad selection; the message is safe to show to the user. */
    public static class InvalidSelectionException extends RuntimeException {
        public InvalidSelectionException(String message) {
            super(message);
        }
    }

    /** Classes that exist AND have Active students, in the order picked (grades outer, sections inner). */
    public List<ClassRecipientCount> preview(Long schoolId, Long mediumId, List<Long> gradeIds, List<Long> sectionIds) {
        List<Long> grades = distinct(gradeIds);
        List<Long> sections = distinct(sectionIds);
        if (grades.isEmpty()) throw new InvalidSelectionException("Please select at least one Grade.");
        if (sections.isEmpty()) throw new InvalidSelectionException("Please select at least one Section.");
        if (grades.size() > MAX_SELECTION || sections.size() > MAX_SELECTION) {
            throw new InvalidSelectionException("Too many grades or sections selected (maximum " + MAX_SELECTION + " of each).");
        }
        if (mediumId != null) {
            boolean ok = false;
            for (Medium m : dropdownService.getMediums()) {
                if (m != null && mediumId.equals(m.getId())) { ok = true; break; }
            }
            if (!ok) throw new InvalidSelectionException("Invalid medium selected.");
        }
        Map<Long, Grade> gradeById = new LinkedHashMap<>();
        for (Long id : grades) {
            Grade g = gradeService.getGradeById(id).orElseThrow(() -> new InvalidSelectionException("Invalid grade selected."));
            gradeById.put(id, g);
        }
        Map<Long, Section> sectionById = new LinkedHashMap<>();
        for (Long id : sections) {
            Section s = sectionService.getSectionById(id).orElseThrow(() -> new InvalidSelectionException("Invalid section selected."));
            sectionById.put(id, s);
        }

        List<Object[]> rows = mediumId == null
                ? academicStudentRepository.countActiveByClasses(schoolId, grades, sections)
                : academicStudentRepository.countActiveByClassesAndMedium(schoolId, mediumId, grades, sections);
        Map<String, Long> counts = new HashMap<>();
        for (Object[] r : rows) {
            counts.put(r[0] + ":" + r[1], ((Number) r[2]).longValue());
        }

        List<ClassRecipientCount> result = new ArrayList<>();
        for (Long gid : grades) {
            for (Long sid : sections) {
                long n = counts.getOrDefault(gid + ":" + sid, 0L);
                if (n > 0) {
                    result.add(new ClassRecipientCount(gid, sid,
                            gradeById.get(gid).getGradeName(), sectionById.get(sid).getSectionName(), n));
                }
            }
        }
        return result;
    }

    /**
     * Sends one message per class. Files (already validated by the caller) are written to disk once
     * and every message points at the same stored file.
     */
    public Map<String, Object> send(School school, UserEntity sender, String heading, String message,
                                    Long mediumId, List<Long> gradeIds, List<Long> sectionIds,
                                    List<MultipartFile> validFiles) throws Exception {
        List<ClassRecipientCount> classes = preview(school.getId(), mediumId, gradeIds, sectionIds);
        if (classes.isEmpty()) {
            throw new InvalidSelectionException("No Active students found for the selected classes.");
        }

        // Files are saved only after the selection is validated.
        List<String[]> storedFiles = new ArrayList<>(); // [storedPath, originalName, contentType]
        List<Long> sizes = new ArrayList<>();
        List<MultipartFile> files = validFiles == null ? Collections.emptyList() : validFiles;
        for (MultipartFile f : files) {
            String stored = fileHandleHelper.saveMessageAttachment(f, school.getId());
            storedFiles.add(new String[]{stored, f.getOriginalFilename(), f.getContentType()});
            sizes.add(f.getSize());
        }

        int sentClasses = 0;
        long sentStudents = 0;
        List<String> failed = new ArrayList<>();
        for (ClassRecipientCount c : classes) {
            try {
                SmsMessage sms = new SmsMessage();
                sms.setSmsHeading(heading);
                sms.setMessageType(SmsMessage.MESSAGE_TYPE_NOTIFICATION);
                sms.setCreatedAt(new Date());
                sms.setSchool(school);
                sms.setCreatedBy(sender);
                sms.setResolution(SmsMessage.RESOLUTION_TYPE_UNRESOLVED);
                sms.setRecipientType(SmsMessage.RECIPIENT_TYPE_CLASS);
                sms.setGrade(gradeService.getGradeById(c.getGradeId()).orElseThrow());
                sms.setSection(sectionService.getSectionById(c.getSectionId()).orElseThrow());

                SmsConversation conversation = new SmsConversation();
                conversation.setSmsMessage(sms);
                conversation.setContent(message);
                conversation.setSentAt(new Date());
                conversation.setSeen(true);
                conversation.setHasAttachment(!storedFiles.isEmpty());
                conversation.setHaveDocAttachment(false);
                conversation.setIsDeleted(false);
                conversation.setInitiatedBy(SmsConversation.INITIATED_BY_SCHOOL);
                sms.setConversations(Collections.singletonList(conversation));

                List<SmsMessageAttachment> attachments = new ArrayList<>();
                for (int i = 0; i < storedFiles.size(); i++) {
                    String[] sf = storedFiles.get(i);
                    attachments.add(new SmsMessageAttachment(sms, sf[0], sf[1], sf[2], sizes.get(i), LocalDateTime.now()));
                }
                sms.setAttachments(attachments);

                smsMessageService.saveSmsMessage(sms);
                if (mediumId == null) {
                    smsMessageService.materializeClassRecipients(sms.getId(), school.getId(), c.getGradeId(), c.getSectionId());
                } else {
                    smsMessageService.materializeClassRecipientsByMedium(sms.getId(), school.getId(), c.getGradeId(), c.getSectionId(), mediumId);
                }
                sentClasses++;
                sentStudents += c.getStudentCount();
            } catch (Exception e) {
                log.error("Multi-class notification failed for class {}", c.getLabel(), e);
                failed.add(c.getLabel());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sentClasses", sentClasses);
        result.put("sentStudents", sentStudents);
        result.put("failedClasses", failed);
        result.put("attachmentCount", storedFiles.size());
        return result;
    }

    private static List<Long> distinct(List<Long> ids) {
        if (ids == null) return Collections.emptyList();
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) set.add(id);
        }
        return new ArrayList<>(set);
    }
}
