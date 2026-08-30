package com.smsweb.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

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

    /**
     * Pins the whole app's request locale to English.
     *
     * Without this, Spring's default AcceptHeaderLocaleResolver derives the
     * locale from the browser's Accept-Language header (or the server JVM's
     * default locale when that header is absent) - and that locale feeds
     * every @DateTimeFormat(pattern = "...") field binding via
     * LocaleContextHolder. Every hand-written date formatter in this app
     * (ExcelService, StudentService, MoneyFormatter, DbBackupService, ...)
     * already explicitly pins Locale.ENGLISH for exactly this reason - but
     * @DateTimeFormat on an entity field has no such override, so
     * AcademicYear.startDate/endDate, Holiday.holidayStartDate/EndDate and
     * ExamDetails.examDeclaredDate (all pattern = "dd/MMM/yyyy", a textual
     * month) were silently at the mercy of whatever locale the request
     * resolved to. Whenever that wasn't English, a value like "04/Sep/2026"
     * failed to bind with "Failed to convert property value of type
     * java.lang.String to required type java.time.LocalDate" - exactly the
     * error seen saving a Holiday.
     *
     * This app has no other locale-sensitive behaviour (no messages bundle,
     * no LocaleContextHolder usage anywhere else, no locale-dependent number
     * formatting) - every UI string is hardcoded English - so fixing the
     * whole app to Locale.ENGLISH just makes that already-assumed behaviour
     * consistent, instead of leaving these 3 fields as the one gap.
     *
     * IF MULTI-LANGUAGE SUPPORT IS EVER ADDED: this bean should be replaced
     * with a real LocaleResolver (cookie/session-based) + a
     * LocaleChangeInterceptor, as normal for Spring i18n. At that point,
     * re-pin AcademicYear.startDate/endDate, Holiday.holidayStartDate/
     * EndDate and ExamDetails.examDeclaredDate's date PARSING to
     * Locale.ENGLISH specifically (e.g. a small AnnotationFormatterFactory
     * for these fields, or switch their pattern to a numeric one like
     * FeeDate.feeSubmissiondate's "dd/MM/yyyy") - the flatpickr date picker
     * feeding them always renders English month abbreviations regardless of
     * UI language, so a non-English request locale would otherwise hit the
     * exact same "Failed to convert ... to LocalDate" error this bean is
     * fixing now, just for users who switch the UI language instead of for
     * everyone.
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.ENGLISH);
    }
}
