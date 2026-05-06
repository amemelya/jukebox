package com.jukebox.backend.controller;

import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.ApiResponse;
import com.jukebox.backend.dto.GoogleLoginRequest;
import com.jukebox.backend.dto.SendOtpRequest;
import com.jukebox.backend.dto.UserProfile;
import com.jukebox.backend.dto.VerifyOtpRequest;
import com.jukebox.backend.service.GoogleAuthService;
import com.jukebox.backend.service.JwtService;
import com.jukebox.backend.service.OtpService;
import com.jukebox.backend.service.OtpService.OtpCreationResult;
import com.jukebox.backend.service.OtpService.OtpVerificationResult;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppProperties appProperties;
    private final OtpService otpService;
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    public AuthController(
            AppProperties appProperties,
            OtpService otpService,
            GoogleAuthService googleAuthService,
            JwtService jwtService
    ) {
        this.appProperties = appProperties;
        this.otpService = otpService;
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpCreationResult result = otpService.createOtpForPhone(request.getPhone());
        String devOtp = appProperties.isExposeDevOtp() && result.isDevOtpAvailable()
                ? result.getCode()
                : null;
        String message = result.isDevOtpAvailable()
                ? "OTP generated successfully."
                : "OTP sent successfully.";

        return ResponseEntity.ok(
                new ApiResponse(true, message, devOtp, null, null)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpVerificationResult result = otpService.verifyOtpForPhone(request.getPhone(), request.getOtp());

        if (!result.isOk()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, result.getMessage(), null, null, null));
        }

        UserProfile user = UserProfile.mobileUser(result.getNormalizedPhone());
        String token = jwtService.issueToken(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Mobile number verified successfully.", null, token, user)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        UserProfile user = googleAuthService.verifyCredential(request.getCredential());
        String token = jwtService.issueToken(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Google sign-in successful.", null, token, user)
        );
    }
}
