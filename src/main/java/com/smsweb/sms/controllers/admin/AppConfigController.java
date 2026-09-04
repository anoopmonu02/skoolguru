package com.smsweb.sms.controllers.admin;

import com.smsweb.sms.services.admin.MaintenanceModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NEW controller (feature: Mobile App Maintenance Mode). Lets ROLE_ADMIN/ROLE_SUPERADMIN
 * take the mobile parent app offline for planned maintenance windows — with a custom
 * message shown on the app's maintenance screen — without a deployment or restart.
 *
 * Hard-locked with @PreAuthorize rather than the flexible @CheckAccess screen-
 * permission system — same precedent as Database Backup / Birthday Notification
 * Settings / Mobile Sessions Cleanup (small system-level admin utilities, not
 * business-data CRUD), and deliberately tighter than that precedent (ROLE_STAFF
 * excluded here) since taking the mobile app offline for every parent/school is
 * far more sensitive than a notification send-time.
 *
 * GET  /admin/app-config          - maintenance mode settings page
 * POST /admin/app-config/toggle   - flip maintenance mode on/off (AJAX)
 * POST /admin/app-config/message  - save the maintenance message shown to parents (AJAX)
 */
@Controller
@RequestMapping("/admin/app-config")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN')")
public class AppConfigController {

    private static final Logger log = LoggerFactory.getLogger(AppConfigController.class);

    private final MaintenanceModeService maintenanceModeService;

    public AppConfigController(MaintenanceModeService maintenanceModeService) {
        this.maintenanceModeService = maintenanceModeService;
    }

    @GetMapping
    public String view(Model model) {
        log.info("Inside app config (maintenance mode) settings page");
        model.addAttribute("maintenanceEnabled", maintenanceModeService.isEnabled());
        model.addAttribute("maintenanceMessage", maintenanceModeService.getMessage());
        model.addAttribute("lastUpdated", maintenanceModeService.getLastUpdated());
        return "admin/appConfig";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam boolean enabled) {
        log.info("Inside app config toggle - enabled={}", enabled);
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            maintenanceModeService.setEnabled(enabled);
            result.put("success", true);
            result.put("enabled", enabled);
            result.put("message", enabled
                    ? "Maintenance mode is now ON — the mobile app is offline for parents."
                    : "Maintenance mode is now OFF — the mobile app is back online.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Could not toggle maintenance mode", e);
            result.put("error", "Could not save — please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/message")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveMessage(@RequestParam String message) {
        log.info("Inside app config saveMessage");
        Map<String, Object> result = new LinkedHashMap<>();
        if (message == null || message.isBlank()) {
            result.put("error", "Please enter a message.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if (message.length() > 255) {
            result.put("error", "Message is too long (max 255 characters).");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        try {
            maintenanceModeService.setMessage(message);
            result.put("success", true);
            result.put("message", "Maintenance message saved.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Could not save maintenance message", e);
            result.put("error", "Could not save — please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
