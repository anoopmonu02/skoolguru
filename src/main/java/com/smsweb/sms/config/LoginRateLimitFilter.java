package com.smsweb.sms.config;

import com.smsweb.sms.services.users.WebLoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rejects POST /login while the username or the caller's IP is locked out,
 * BEFORE Spring Security attempts authentication.
 *
 * Doing it in a filter rather than inside UserDetailsService matters: the
 * password is never checked while locked, so an attacker gets no timing signal
 * about whether the username exists, and the database is not touched at all
 * during a flood.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final WebLoginAttemptService attempts;

    public LoginRateLimitFilter(WebLoginAttemptService attempts) {
        this.attempts = attempts;
    }

    /**
     * Honours X-Forwarded-For because the app sits behind a reverse proxy — without
     * this every request would look like it came from the proxy's own address and
     * the per-IP counter would lock the whole school out at once.
     */
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();     // left-most entry is the original client
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        boolean isLoginPost = "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/login");

        if (isLoginPost) {
            String username = request.getParameter("username");
            String ip       = clientIp(request);
            if (attempts.isBlocked(username, ip)) {
                long mins = attempts.remainingMinutes(username, ip);
                response.sendRedirect(request.getContextPath() + "/login?locked=" + mins);
                return;                          // password never checked, DB never queried
            }
        }
        chain.doFilter(request, response);
    }
}
