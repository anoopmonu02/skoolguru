package com.smsweb.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Previously registered "/images/employees/**" and "/images/students/**" as
 * PUBLIC static resource mappings straight onto the filesystem folders that
 * hold student/employee photos — combined with the permitAll rule in
 * WebSecurityConfig, that meant anyone (no login required) could view any
 * student or employee's photo just by guessing/knowing the filename.
 *
 * Removed as a security fix. Photos are now served exclusively through the
 * existing authenticated, permission-checked (@CheckAccess) endpoints:
 *   GET /student/images/{filename}   (StudentController)
 *   GET /employee/images/{filename}  (EmployeeController)
 * which also sanitise the filename against path traversal.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // No custom static resource mappings.
    }

    /**
     * Spring Boot 4 change: the `server.forward-headers-strategy` property is NO
     * LONGER EFFECTIVE. Registering this filter manually is the documented
     * replacement.
     *
     * Why it matters here: the app runs behind a reverse proxy under the context
     * path /sms. Without it, Spring builds redirect URLs (login success, logout,
     * access-denied, fee-receipt redirects) from the INTERNAL request - so it emits
     * http://internal-host:9090/... instead of https://your-domain/sms/... Users get
     * redirected somewhere unreachable, or bounced from https to http.
     *
     * Invisible in local development, because there is no proxy in front. It only
     * shows up once deployed - which is exactly why it is easy to miss.
     *
     * HIGHEST_PRECEDENCE so the headers are applied before Spring Security builds
     * any redirect of its own.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
