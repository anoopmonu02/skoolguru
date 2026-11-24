package com.smsweb.sms.services.student;

import com.smsweb.sms.dto.FamilyMappingRowDto;
import com.smsweb.sms.models.student.FamilyAccount;
import com.smsweb.sms.repositories.student.FamilyAccountRepository;
import com.smsweb.sms.repositories.student.FamilyMappingQueryRepository;
import com.smsweb.sms.services.mobile.FamilyAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin > Family Mapping. Deliberately NOT @Transactional: Map all runs one transaction per
 * distinct mobile1 (inside FamilyAccountService.createIfAbsent), so one bad number can never
 * undo the others. Rules: only mobile1 links a family; an INACTIVE account is never
 * auto-activated (shown as Mismatch, skipped on Map / Re-map).
 */
@Service
public class FamilyMappingService {
    private static final Logger log = LoggerFactory.getLogger(FamilyMappingService.class);

    private final FamilyMappingQueryRepository queryRepository;
    private final FamilyAccountRepository familyAccountRepository;
    private final FamilyAccountService familyAccountService;

    public FamilyMappingService(FamilyMappingQueryRepository queryRepository,
                                FamilyAccountRepository familyAccountRepository,
                                FamilyAccountService familyAccountService) {
        this.queryRepository = queryRepository;
        this.familyAccountRepository = familyAccountRepository;
        this.familyAccountService = familyAccountService;
    }

    public static boolean isValidMobile(String m) {
        return m != null && m.matches("\\d{10}");
    }

    /** Works out MAPPED / MAP / MISMATCH / MISSING for one row. */
    public static String computeState(FamilyMappingRowDto r) {
        if (!isValidMobile(r.getMobile1())) return FamilyMappingRowDto.MISSING;
        if (r.getFamilyAccountId() == null) return FamilyMappingRowDto.MAP;
        boolean ok = "ACTIVE".equalsIgnoreCase(r.getFamilyStatus()) && r.getMobile1().equals(r.getFamilyMobile());
        return ok ? FamilyMappingRowDto.MAPPED : FamilyMappingRowDto.MISMATCH;
    }

    public List<FamilyMappingRowDto> getRows(Long schoolId, Long yearId, Long mediumId) {
        List<FamilyMappingRowDto> rows = queryRepository.findRows(schoolId, yearId, mediumId);
        for (FamilyMappingRowDto r : rows) {
            r.setState(computeState(r));
        }
        return rows;
    }

    public Map<String, Object> summarize(List<FamilyMappingRowDto> rows) {
        long mapped = 0, pending = 0, missing = 0;
        for (FamilyMappingRowDto r : rows) {
            String st = r.getState() != null ? r.getState() : computeState(r);
            if (FamilyMappingRowDto.MAPPED.equals(st)) mapped++;
            else if (FamilyMappingRowDto.MISSING.equals(st)) missing++;
            else pending++;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", (long) rows.size());
        m.put("mapped", mapped);
        m.put("pending", pending);
        m.put("missing", missing);
        return m;
    }

    private boolean targetAccountInactive(String mobile1) {
        return familyAccountRepository.findByMobile(mobile1)
                .map(FamilyAccount::getStatus)
                .map(st -> !"ACTIVE".equalsIgnoreCase(st))
                .orElse(false);
    }

    /** Map / Re-map one student. Mobile 1 is read from the database, never from the request. */
    public Map<String, Object> mapOne(Long academicStudentId, Long schoolId, Long yearId) {
        Map<String, Object> res = new LinkedHashMap<>();
        List<FamilyMappingRowDto> found = queryRepository.findRowByAcademicStudentId(academicStudentId, schoolId, yearId);
        if (found.isEmpty()) {
            res.put("error", "Student not found in the current school and session.");
            return res;
        }
        FamilyMappingRowDto row = found.get(0);
        String state = computeState(row);
        if (FamilyMappingRowDto.MISSING.equals(state)) {
            res.put("error", "Mobile 1 is missing or is not a 10-digit number. Add it on Edit Student first.");
            return res;
        }
        if (FamilyMappingRowDto.MAPPED.equals(state)) {
            res.put("success", true);
            res.put("message", "Already mapped.");
            return res;
        }
        if (targetAccountInactive(row.getMobile1())) {
            res.put("error", "The family account for this mobile number is INACTIVE. It is not activated automatically; reactivate it first.");
            return res;
        }
        try {
            familyAccountService.createIfAbsent(row.getMobile1());
        } catch (Exception e) {
            log.error("Family mapping failed for academicStudentId={}", academicStudentId, e);
            res.put("error", "Could not map this student right now. Please try again.");
            return res;
        }
        List<FamilyMappingRowDto> after = queryRepository.findRowByAcademicStudentId(academicStudentId, schoolId, yearId);
        if (!after.isEmpty() && FamilyMappingRowDto.MAPPED.equals(computeState(after.get(0)))) {
            res.put("success", true);
            res.put("message", "Mapped to the family account of " + row.getMobile1() + ".");
        } else {
            res.put("error", "The link could not be confirmed. Please refresh and check this student.");
        }
        return res;
    }

    /** Map all pending students of one medium: recomputed on the server, one transaction per distinct mobile1. */
    public Map<String, Object> mapAll(Long schoolId, Long yearId, Long mediumId) {
        List<FamilyMappingRowDto> rows = getRows(schoolId, yearId, mediumId);
        long pendingBefore = 0;
        Set<String> numbers = new LinkedHashSet<>();
        for (FamilyMappingRowDto r : rows) {
            if (FamilyMappingRowDto.MAP.equals(r.getState()) || FamilyMappingRowDto.MISMATCH.equals(r.getState())) {
                pendingBefore++;
                numbers.add(r.getMobile1());
            }
        }
        int failedFamilies = 0;
        int skippedInactive = 0;
        List<String> failedNumbers = new ArrayList<>();
        for (String mobile : numbers) {
            try {
                if (targetAccountInactive(mobile)) {
                    skippedInactive++;
                    continue;
                }
                familyAccountService.createIfAbsent(mobile);
            } catch (Exception e) {
                failedFamilies++;
                if (failedNumbers.size() < 10) failedNumbers.add(mobile);
                log.error("Map all: failed for mobile {}", mobile, e);
            }
        }
        List<FamilyMappingRowDto> after = getRows(schoolId, yearId, mediumId);
        Map<String, Object> summary = summarize(after);
        long pendingAfter = ((Number) summary.get("pending")).longValue();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("mappedNow", Math.max(0, pendingBefore - pendingAfter));
        res.put("skippedInactiveFamilies", skippedInactive);
        res.put("failedFamilies", failedFamilies);
        res.put("failedNumbers", failedNumbers);
        res.put("summary", summary);
        return res;
    }
}
