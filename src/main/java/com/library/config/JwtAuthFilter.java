package com.library.config;

import com.library.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Fully public paths — any HTTP method allowed without auth
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Read-only public paths — only GET/HEAD allowed without auth
        if (isReadOnlyPublicPath(path) && ("GET".equals(method) || "HEAD".equals(method))) {
            chain.doFilter(request, response);
            return;
        }

        // 3. All other paths (including write operations on read-only paths) require auth
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
            return;
        }

        // Set user info in request attributes
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("userRole", jwtUtil.getRoleFromToken(token));
        chain.doFilter(request, response);
    }

    /**
     * Fully public paths — completely open, all HTTP methods.
     */
    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/health")
                || path.equals("/")
                || path.startsWith("/covers/");
    }

    /**
     * Read-only public paths — GET/HEAD allowed without auth.
     * POST/PUT/DELETE on these paths require authentication.
     */
    private boolean isReadOnlyPublicPath(String path) {
        return path.equals("/api/facets")
                || path.startsWith("/api/books")
                || path.startsWith("/api/categories")
                || path.startsWith("/api/rules")
                || path.startsWith("/api/holds/count");
    }
}
