package com.smsweb.sms.services.admin;

import com.smsweb.sms.models.admin.SystemConfig;
import com.smsweb.sms.repositories.admin.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NEW service (feature: Mobile App Maintenance Mode). Lets ROLE_ADMIN/ROLE_SUPERADMIN
 * take the mobile parent app offline for planned maintenance windows, with a custom
 * message shown to parents, without a deployment or server restart.
 *
 * Backed by the existing global {@link SystemConfig} key-value table (same
 * precedent as {@code DbBackupService} / Birthday Notification Settings):
 *   - MAINTENANCE_MODE_ENABLED  : "true" / "false"
 *   - MAINTENANCE_MODE_MESSAGE  : free-text message shown on the mobile app's
 *                                 maintenance screen
 *
 * The flag is read on EVERY mobile API request (via MaintenanceModeFilter), so it is
 * cached in memory (AtomicBoolean/AtomicReference) rather than hitting the database
 * per-request. The cache is refreshed:
 *   1. On startup (@PostConstruct)
 *   2. Every 30s in the background (@Scheduled — SmsApplication already has
 *      @EnableScheduling, so no further config is needed)
 *   3. Immediately/synchronously whenever AppConfigController saves a change, so a
 *      toggle flipped in the admin UI takes effect on the very next mobile request
 *      instead of waiting up to 30s for the next scheduled refresh.
 *
 * Fail-open by design: if the database lookup throws (e.g. a transient DB hiccup),
 * the cached value is simply left unchanged and the error is logged — a maintenance
 * mode outage must never itself be the thing that takes the mobile app down.
 */
@Service
public class MaintenanceModeService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceModeService.class);

    public static final String CONFIG_ENABLED = "MAINTENANCE_MODE_ENABLED";
    public static final String CONFIG_MESSAGE = "MAINTENANCE_MODE_MESSAGE";

    public static final String DEFAULT_MESSAGE =
            "We're currently performing scheduled maintenance. Please try again shortly.";

    private final SystemConfigRepository systemConfigRepository;

    // Cached view of the two SystemConfig rows above — read by the filter on every
    // mobile API request, so this must never itself hit the database.
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicReference<String> message = new AtomicReference<>(DEFAULT_MESSAGE);

    public MaintenanceModeService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostConstruct
    public void init() {
        refreshFromDb();
    }

    /** Re-reads both SystemConfig rows into the in-memory cache. Fail-open on error. */
    @Scheduled(fixedRate = 30_000)
    public void refreshFromDb() {
        try {
            boolean dbEnabled = systemConfigRepository.findByConfigName(CONFIG_ENABLED)
                    .map(SystemConfig::getConfigValue)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            String dbMessage = systemConfigRepository.findByConfigName(CONFIG_MESSAGE)
                    .map(SystemConfig::getConfigValue)
                    .filter(v -> v != null && !v.isBlank())
                    .orElse(DEFAULT_MESSAGE);

            enabled.set(dbEnabled);
            message.set(dbMessage);
        } catch (Exception e) {
            // Fail-open: keep serving whatever was last cached rather than risk
            // flipping the whole mobile app offline because of an unrelated DB blip.
            log.warn("Could not refresh maintenance-mode config from DB — keeping cached value (enabled={})",
                    enabled.get(), e);
        }
    }

    /** Cheap, allocation-free check — safe to call on every mobile API request. */
    public boolean isEnabled() {
        return enabled.get();
    }

    public String getMessage() {
        return message.get();
    }

    /** When the ON/OFF toggle was last changed, or null if it has never been set. */
    public java.util.Date getLastUpdated() {
        return systemConfigRepository.findByConfigName(CONFIG_ENABLED)
                .map(SystemConfig::getUpdatedDate)
                .orElse(null);
    }

    /** Persists the toggle and refreshes the cache immediately (no 30s wait). */
    public void setEnabled(boolean value) {
        saveConfig(CONFIG_ENABLED, Boolean.toString(value),
                "Whether the mobile parent app is in maintenance mode (blocks all /api/v1/** traffic except /api/v1/auth/ping). Set from the App Config admin screen.");
        refreshFromDb();
    }

    /** Persists the maintenance message and refreshes the cache immediately. */
    public void setMessage(String value) {
        String trimmed = (value == null || value.isBlank()) ? DEFAULT_MESSAGE : value.trim();
        saveConfig(CONFIG_MESSAGE, trimmed,
                "Message shown on the mobile app's maintenance screen while maintenance mode is on. Set from the App Config admin screen.");
        refreshFromDb();
    }

    private void saveConfig(String key, String value, String description) {
        SystemConfig config = systemConfigRepository.findByConfigName(key).orElseGet(() -> {
            SystemConfig c = new SystemConfig();
            c.setConfigName(key);
            return c;
        });
        config.setConfigValue(value);
        config.setDescription(description);
        systemConfigRepository.save(config);
    }
}
