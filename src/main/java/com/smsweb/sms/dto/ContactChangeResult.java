package com.smsweb.sms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of the two-call "Update Contact" flow on Fee Submission. Exactly one of
 * error / noChange / needsConfirmation / success(message) is set.
 */
@Getter
@Setter
public class ContactChangeResult {
    private String error;
    private String successMessage;
    private boolean noChange;
    private boolean needsConfirmation;
    /** NEW_FAMILY or EXISTING_FAMILY */
    private String scenario;
    private String message;
    /** Children of THIS school already on the number (name + class). */
    private List<Map<String, String>> children;
    private int familyChildCount;
    private int otherBranchCount;

    public static ContactChangeResult error(String msg) {
        ContactChangeResult r = new ContactChangeResult();
        r.error = msg;
        return r;
    }

    public static ContactChangeResult noChange(String msg) {
        ContactChangeResult r = new ContactChangeResult();
        r.noChange = true;
        r.message = msg;
        return r;
    }

    /** Keys the browser reads: error, success, noChange, needsConfirmation, scenario, message, children, familyChildCount, otherBranchCount. */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (error != null) {
            m.put("error", error);
            return m;
        }
        if (successMessage != null) {
            m.put("success", successMessage);
            return m;
        }
        if (noChange) {
            m.put("noChange", true);
            m.put("message", message);
            return m;
        }
        if (needsConfirmation) {
            m.put("needsConfirmation", true);
            m.put("scenario", scenario);
            m.put("message", message);
            m.put("children", children);
            m.put("familyChildCount", familyChildCount);
            m.put("otherBranchCount", otherBranchCount);
        }
        return m;
    }
}
