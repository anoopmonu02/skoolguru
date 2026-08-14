package com.smsweb.sms.helper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Indian-format currency rendering, exposed to Thymeleaf as the bean "money".
 *
 * Usage in a template:
 *     th:text="${@money.inr(feeSubmission.totalAmount)}"      ->  1,25,000.00
 *     th:text="${@money.rupees(feeSubmission.totalAmount)}"   ->  ₹ 1,25,000.00
 *
 * Why this exists: amounts were rendered straight from BigDecimal.toString(),
 * producing "125000.00" with no digit grouping whatsoever. Anyone handling money
 * daily reads that as wrong, and it makes large figures genuinely hard to scan.
 *
 * Why not Thymeleaf's #numbers.formatDecimal(...): its COMMA grouping produces
 * the Western pattern 125,000.00. India groups the last three digits and then in
 * pairs — 1,25,000.00 (one lakh twenty-five thousand). The "#,##,##0.00" pattern
 * below encodes exactly that, and unlike relying on the en-IN locale it does not
 * depend on which CLDR data the JVM happens to ship.
 */
@Component("money")
public class MoneyFormatter {

    /** Indian grouping: groups of 2 after the first group of 3. */
    private static final String PATTERN = "#,##,##0.00";

    private DecimalFormat formatter() {
        // DecimalFormat is NOT thread-safe, so build one per call. These are
        // cheap and only used during view rendering.
        return new DecimalFormat(PATTERN, DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    }

    /** 125000 -> "1,25,000.00". Null-safe: returns "0.00". */
    public String inr(BigDecimal amount) {
        return formatter().format(amount == null ? BigDecimal.ZERO : amount);
    }

    public String inr(Number amount) {
        return formatter().format(amount == null ? 0 : amount);
    }

    /** Same, prefixed with the rupee sign. */
    public String rupees(BigDecimal amount) {
        return "₹ " + inr(amount);
    }

    public String rupees(Number amount) {
        return "₹ " + inr(amount);
    }
}
