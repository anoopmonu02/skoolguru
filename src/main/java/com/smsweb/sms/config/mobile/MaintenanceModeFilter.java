package com.smsweb.sms.config.mobile;

import com.smsweb.sms.services.admin.MaintenanceModeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Maintenance-mode gate for the Mobile API (/api/v1/**).
 *
 * Registered in WebSecurityConfig's mobile chain (@Order 1), BEFORE
 * JwtAuthenticationFilter — so a maintenance-mode block happens before any token
 * is even parsed, and applies equally to logged-out (login) and logged-in requests.
 *
 * /api/v1/auth/ping is deliberately exempt: it is the one endpoint the mobile app's
 * maintenance screen polls (via a "Try Again" button) to find out when maintenance
 * mode has been turned back off, so it must always be reachable — including while
 * maintenance mode is on.
 *
 * Reads only the in-memory cache on MaintenanceModeService (never the database
 * directly), so this filter adds negligible overhead per request.
 */
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceModeFilter.class);

    private static final String PING_PATH = "/api/v1/auth/ping";

    private final MaintenanceModeService maintenanceModeService;

    public MaintenanceModeFilter(MaintenanceModeService maintenanceModeService) {
        this.maintenanceModeService = maintenanceModeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path != null && path.endsWith(PING_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (maintenanceModeService.isEnabled()) {
                String message = maintenanceModeService.getMessage();
                log.debug("Blocking {} — mobile app is in maintenance mode", path);

                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"" + escapeJson(message) + "\",\"data\":{\"maintenanceMode\":true}}");
                return;
            }
        } catch (Exception e) {
            // Fail-open: a bug/blip in the maintenance-mode check itself must never
            // block real mobile traffic. Log and let the request proceed normally.
            log.warn("Maintenance-mode check failed — failing open (allowing request through): {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
