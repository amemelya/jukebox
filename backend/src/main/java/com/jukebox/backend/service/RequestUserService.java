package com.jukebox.backend.service;

import com.jukebox.backend.dto.UserProfile;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestUserService {

    private final JwtService jwtService;
    private final UserService userService;

    public RequestUserService(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public com.jukebox.backend.model.UserAccount requireUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization bearer token is required.");
        }

        UserProfile profile = jwtService.parseToken(authorization.substring(7));
        return userService.getOrCreateUser(profile);
    }
}
