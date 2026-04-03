package dev.waqas.audit.web;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.waqas.audit.AuditMdcKeys;
import dev.waqas.audit.autoconfigure.AuditProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Copies selected HTTP headers into MDC for {@link dev.waqas.audit.MdcAuditContextProvider}.
 * Registered as a bean from {@link dev.waqas.audit.autoconfigure.AuditAutoConfiguration} when servlet API is present.
 */
public class AuditMdcFilter extends OncePerRequestFilter {

    private final AuditProperties properties;

    public AuditMdcFilter(AuditProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            putIfPresent(AuditMdcKeys.CORRELATION_ID, header(request, properties.getMdc().getCorrelationIdHeader()));
            putIfPresent(AuditMdcKeys.SESSION_ID, header(request, properties.getMdc().getSessionIdHeader()));
            putIfPresent(AuditMdcKeys.USER_ID, header(request, properties.getMdc().getUserIdHeader()));
            if (MDC.get(AuditMdcKeys.CLIENT_IP) == null) {
                MDC.put(AuditMdcKeys.CLIENT_IP, request.getRemoteAddr());
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(AuditMdcKeys.CORRELATION_ID);
            MDC.remove(AuditMdcKeys.SESSION_ID);
            MDC.remove(AuditMdcKeys.USER_ID);
            MDC.remove(AuditMdcKeys.CLIENT_IP);
        }
    }

    private static String header(HttpServletRequest request, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return request.getHeader(name);
    }

    private static void putIfPresent(String mdcKey, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(mdcKey, value);
        }
    }
}
