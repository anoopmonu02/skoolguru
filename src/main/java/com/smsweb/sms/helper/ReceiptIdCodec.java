package com.smsweb.sms.helper;

import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Encodes/decodes FeeSubmission ids for use in URLs
 * (/fees/receipt-print/{id}, /fees/student-receipt-print/{id}), so the
 * numeric id isn't directly visible or incrementable in the address bar.
 *
 * This is obfuscation, not access control: Hashids is reversible by anyone
 * who has (or brute-forces) the configured salt, and even without the salt
 * it does nothing to stop someone using the app's own UI to reach any record
 * they're already authorized to see. The actual access-control boundary is
 * the school-ownership check in
 * FeeSubmissionService#getFeeReceiptData - this class only makes casual
 * guessing/incrementing of the id in the URL bar impractical.
 */
@Component
public class ReceiptIdCodec {

    private static final Logger log = LoggerFactory.getLogger(ReceiptIdCodec.class);

    private final Hashids hashids;

    public ReceiptIdCodec(@Value("${app.hashids.salt}") String salt) {
        // minLength 8 so small ids (e.g. id=1) don't produce a visibly-short,
        // obviously-low encoded string.
        this.hashids = new Hashids(salt, 8);
    }

    public String encode(Long id) {
        if (id == null) {
            return null;
        }
        return hashids.encode(id);
    }

    /**
     * Returns null for anything that doesn't decode to exactly one
     * non-negative id - a malformed, tampered, truncated, or foreign-salt
     * string included - so callers can treat it exactly like "id not found"
     * instead of throwing.
     */
    public Long decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            long[] decoded = hashids.decode(encoded);
            if (decoded.length != 1) {
                return null;
            }
            return decoded[0];
        } catch (Exception e) {
            log.debug("ReceiptIdCodec: could not decode value (treating as not-found): {}", encoded);
            return null;
        }
    }
}
