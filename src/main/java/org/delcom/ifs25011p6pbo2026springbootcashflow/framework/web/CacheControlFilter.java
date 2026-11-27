package org.delcom.ifs25011p6pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Cache panjang untuk bundel frontend Vaadin ber-hash (/VAADIN/build/**), memperbaiki audit "efficient cache policy". */
@Component
public class CacheControlFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/VAADIN/build/")) {
            response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        }
        chain.doFilter(request, response);
    }
}
