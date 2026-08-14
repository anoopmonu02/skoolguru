package com.smsweb.sms.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import com.smsweb.sms.services.users.WebLoginAttemptService;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    private final WebLoginAttemptService attempts;

    public CustomAuthenticationFailureHandler(WebLoginAttemptService attempts) {
        this.attempts = attempts;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("Inside onAuthenticationFailure");
        request.getSession().setAttribute("errorMessage", exception.getMessage());
        log.warn("Authentication failed: {}", exception.getMessage());

        // Count this failure against both the username and the IP. Unknown
        // usernames count too - otherwise probing for valid accounts is free.
        attempts.recordFailure(request.getParameter("username"), clientIp(request));
        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }


}
