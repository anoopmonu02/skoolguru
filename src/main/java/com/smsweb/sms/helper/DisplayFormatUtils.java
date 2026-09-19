package com.smsweb.sms.helper;

/**
 * Small, stateless text-formatting helpers for server-rendered (non-DataTables)
 * pages that still want the same "avatar initials" and "masked contact number"
 * look used elsewhere in the app (see static/css/components.css: .stu-avatar,
 * .mask-cell, and student.html's client-side initialsOf()/maskMobile() for the
 * original DataTables version of this pattern).
 *
 * Called from Thymeleaf via T(com.smsweb.sms.helper.DisplayFormatUtils).method(...).
 * Kept dependency-free (no Spring/JPA imports) so it stays trivially unit-testable
 * and safe to call from any template.
 */
public final class DisplayFormatUtils {

    private static final String[] AVATAR_COLORS = {
            "#0ea5e9", "#7c3aed", "#d97706", "#16a34a", "#dc2626", "#0891b2"
    };

    private DisplayFormatUtils() {
    }

    /**
     * First letter of the first word + first letter of the last word, uppercased
     * (e.g. "Prisha Gupta" -> "PG", "Prisha" -> "P", null/blank -> ""). Splits on
     * any run of whitespace so irregular spacing in real-world name data doesn't
     * produce blank initials.
     */
    public static String initials(String fullName) {
        if (fullName == null) {
            return "";
        }
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] words = trimmed.split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(words[0].charAt(0)));
        if (words.length > 1) {
            String last = words[words.length - 1];
            if (!last.isEmpty()) {
                sb.append(Character.toUpperCase(last.charAt(0)));
            }
        }
        return sb.toString();
    }

    /**
     * Deterministic accent color per person, keyed off their numeric id cycling
     * through a fixed palette (same palette/approach as student.html's
     * avatarColors + "row.id % colors.length"). Null id falls back to the first
     * color rather than throwing.
     */
    public static String avatarColor(Long id) {
        if (id == null) {
            return AVATAR_COLORS[0];
        }
        int idx = (int) (Math.abs(id) % AVATAR_COLORS.length);
        return AVATAR_COLORS[idx];
    }

    /**
     * Real (not cosmetic) display masking: returns ONLY the masked string, so —
     * unlike the client-side mask on Student List, where the full value still
     * ships in the page's AJAX JSON and sits in a data-full attribute in the DOM
     * — the unmasked number is never present in this page's HTML at all. Shows
     * the first 2 and last 2 digits, masks the rest with 'x'; same visual format
     * as the rest of the app. Values of 4 chars or fewer are returned as-is
     * (nothing meaningful left to mask).
     */
    public static String maskMobile(String mobile) {
        if (mobile == null) {
            return "";
        }
        String v = mobile.trim();
        if (v.length() <= 4) {
            return v;
        }
        StringBuilder masked = new StringBuilder();
        masked.append(v, 0, 2);
        for (int i = 2; i < v.length() - 2; i++) {
            masked.append('x');
        }
        masked.append(v.substring(v.length() - 2));
        return masked.toString();
    }
}
