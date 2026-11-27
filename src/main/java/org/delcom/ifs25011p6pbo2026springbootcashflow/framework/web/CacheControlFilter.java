package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Cache panjang untuk bundel frontend Vaadin ber-hash (/VAADIN/build/**).
 * Header Cache-Control/Expires dari handler lain diabaikan agar tidak menimpa nilai ini.
 */
@Component
public class CacheControlFilter extends OncePerRequestFilter {

    private static final String LONG_CACHE = "public, max-age=31536000, immutable";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/VAADIN/build/")) {
            chain.doFilter(request, response);
            return;
        }
        response.setHeader("Cache-Control", LONG_CACHE);
        chain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if (!isCacheHeader(name)) {
                    super.setHeader(name, value);
                }
            }

            @Override
            public void addHeader(String name, String value) {
                if (!isCacheHeader(name)) {
                    super.addHeader(name, value);
                }
            }

            @Override
            public void setDateHeader(String name, long date) {
                if (!isCacheHeader(name)) {
                    super.setDateHeader(name, date);
                }
            }

            @Override
            public void addDateHeader(String name, long date) {
                if (!isCacheHeader(name)) {
                    super.addDateHeader(name, date);
                }
            }
        });
    }

    private static boolean isCacheHeader(String name) {
        return "Cache-Control".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name);
    }
}
