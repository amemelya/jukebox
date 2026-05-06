package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AdminAccessService {

    private final AppProperties appProperties;

    public AdminAccessService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void requireAdmin(HttpServletRequest request) {
        String suppliedSecret = request.getHeader("X-Admin-Secret");
        if (suppliedSecret == null || !suppliedSecret.equals(appProperties.getAdminSecret())) {
            throw new IllegalArgumentException("Valid admin secret is required.");
        }
    }
}
