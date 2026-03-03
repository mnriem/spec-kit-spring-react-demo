package com.llmanalytics.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final byte[] expectedKeyHash;

    public ApiKeyAuthFilter(@Value("${app.security.api-key}") String apiKey) {
        this.expectedKeyHash = hashKey(apiKey);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // Only apply to /api/** endpoints
        if (!requestUri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Actuator health endpoint is public
        if (requestUri.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);
        if (providedKey != null && isValidKey(providedKey)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "api-client", null,
                    List.of(new SimpleGrantedAuthority("ROLE_API")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid API key\",\"timestamp\":\""
                    + java.time.Instant.now() + "\"}");
        }
    }

    private boolean isValidKey(String providedKey) {
        byte[] providedHash = hashKey(providedKey);
        return MessageDigest.isEqual(expectedKeyHash, providedHash);
    }

    private static byte[] hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
