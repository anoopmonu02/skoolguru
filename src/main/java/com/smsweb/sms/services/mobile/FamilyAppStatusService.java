package com.smsweb.sms.services.mobile;

import com.smsweb.sms.repositories.mobile.FamilyAppUsageQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Parent mobile-app status of one student, for the hint on Fee Submission.
 * REQUIRES_NEW + read-only on purpose: the fee-screen endpoint is itself @Transactional, and a
 * failure in here must not mark that whole transaction rollback-only (it would break the screen).
 * "USING" means a login record (refresh token) exists for any child of the family. Old records
 * are cleaned up daily, so NO_LOGIN really means "no recent app login found".
 */
@Service
public class FamilyAppStatusService {

    public static final String NO_ACCOUNT = "NO_ACCOUNT";
    public static final String NO_LOGIN = "NO_LOGIN";
    public static final String USING = "USING";

    private final FamilyAppUsageQueryRepository repository;

    public FamilyAppStatusService(FamilyAppUsageQueryRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public String statusForStudent(Long studentId) {
        List<Long> ids = repository.findFamilyAccountIdOfStudent(studentId);
        if (ids == null || ids.isEmpty() || ids.get(0) == null) return NO_ACCOUNT;
        return repository.countTokensOfFamily(ids.get(0)) > 0 ? USING : NO_LOGIN;
    }
}
