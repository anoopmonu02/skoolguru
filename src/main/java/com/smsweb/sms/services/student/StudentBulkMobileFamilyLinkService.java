package com.smsweb.sms.services.student;

import com.smsweb.sms.dto.BulkMobileLinkRow;
import com.smsweb.sms.repositories.student.FamilyAccountRepository;
import com.smsweb.sms.repositories.student.FamilyMappingQueryRepository;
import com.smsweb.sms.services.mobile.FamilyAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Runs AFTER Bulk Update "Mobile 1, Mobile 2" has committed. Deliberately NOT @Transactional:
 * every {@code createIfAbsent} is its own transaction, so one failing number cannot undo the
 * saved rows or the other numbers. A row that is still not linked correctly keeps its saved
 * mobile and gets {@code warning=true} (Admin > Family Mapping can fix it later).
 */
@Service
public class StudentBulkMobileFamilyLinkService {
    private static final Logger log = LoggerFactory.getLogger(StudentBulkMobileFamilyLinkService.class);

    private static final String NOT_LINKED =
            "Saved, but not linked to a family account. Use Admin > Family Mapping to fix it.";
    private static final String INACTIVE_ACCOUNT =
            "Saved, but the family account for this number is INACTIVE and was not changed. Use Admin > Family Mapping to review it.";

    private final FamilyMappingQueryRepository queryRepository;
    private final FamilyAccountRepository familyAccountRepository;
    private final FamilyAccountService familyAccountService;

    public StudentBulkMobileFamilyLinkService(FamilyMappingQueryRepository queryRepository,
                                              FamilyAccountRepository familyAccountRepository,
                                              FamilyAccountService familyAccountService) {
        this.queryRepository = queryRepository;
        this.familyAccountRepository = familyAccountRepository;
        this.familyAccountService = familyAccountService;
    }

    /** @param results the per-row results returned by saveFieldGroup (mutated: warning/message). */
    public void applyFamilyLink(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) return;

        // 1. uuid -> student id for the rows that saved
        Map<UUID, Map<String, Object>> savedByUuid = new LinkedHashMap<>();
        for (Map<String, Object> r : results) {
            if (!Boolean.TRUE.equals(r.get("success"))) continue;
            try {
                savedByUuid.put(UUID.fromString(String.valueOf(r.get("uuid"))), r);
            } catch (IllegalArgumentException ignored) {
                // not a uuid: nothing to link
            }
        }
        if (savedByUuid.isEmpty()) return;
        Map<UUID, Long> studentIdByUuid = new HashMap<>();
        for (Object[] row : queryRepository.findStudentIdsByAcademicUuids(savedByUuid.keySet())) {
            studentIdByUuid.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        if (studentIdByUuid.isEmpty()) return;

        // 2. which saved students are not linked correctly yet
        Map<Long, BulkMobileLinkRow> before = indexByStudent(queryRepository.findLinkRows(studentIdByUuid.values()));
        Set<String> toLink = new LinkedHashSet<>();
        Set<String> inactiveNumbers = new HashSet<>();
        for (BulkMobileLinkRow row : before.values()) {
            if (row.isLinkedCorrectly() || !FamilyMappingService.isValidMobile(row.getMobile1())) continue;
            toLink.add(row.getMobile1());
        }
        for (String mobile : toLink) {
            try {
                boolean inactive = familyAccountRepository.findByMobile(mobile)
                        .map(a -> !"ACTIVE".equalsIgnoreCase(a.getStatus())).orElse(false);
                if (inactive) {
                    inactiveNumbers.add(mobile);   // never auto-activate
                    continue;
                }
                familyAccountService.createIfAbsent(mobile);
            } catch (Exception e) {
                log.error("Bulk mobile update: could not link family for {}", mobile, e);
            }
        }

        // 3. confirm, and warn on every row that is still not linked
        Map<Long, BulkMobileLinkRow> after = indexByStudent(queryRepository.findLinkRows(studentIdByUuid.values()));
        for (Map.Entry<UUID, Map<String, Object>> e : savedByUuid.entrySet()) {
            Long studentId = studentIdByUuid.get(e.getKey());
            BulkMobileLinkRow row = studentId == null ? null : after.get(studentId);
            if (row == null || row.isLinkedCorrectly()) continue;
            e.getValue().put("warning", true);
            e.getValue().put("message", inactiveNumbers.contains(row.getMobile1()) ? INACTIVE_ACCOUNT : NOT_LINKED);
        }
    }

    private static Map<Long, BulkMobileLinkRow> indexByStudent(List<BulkMobileLinkRow> rows) {
        Map<Long, BulkMobileLinkRow> m = new HashMap<>();
        for (BulkMobileLinkRow r : rows) m.put(r.getStudentId(), r);
        return m;
    }
}
