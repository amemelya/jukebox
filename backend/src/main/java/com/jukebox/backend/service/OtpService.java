package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class OtpService {

    private static final long OTP_TTL_SECONDS = 300L;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final Map<String, OtpRecord> otpRecords = new ConcurrentHashMap<String, OtpRecord>();
    private final SecureRandom random = new SecureRandom();
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    public OtpService(AppProperties appProperties, RestTemplateBuilder restTemplateBuilder) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplateBuilder.build();
    }

    public OtpCreationResult createOtpForPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);

        if ("twilio".equalsIgnoreCase(appProperties.getSmsProvider())) {
            sendTwilioVerification(normalizedPhone);
            return new OtpCreationResult(null, 0L, normalizedPhone);
        }

        String code = generateOtp();
        long expiresAt = System.currentTimeMillis() + (OTP_TTL_SECONDS * 1000L);
        otpRecords.put(normalizedPhone, new OtpRecord(code, expiresAt));
        return new OtpCreationResult(code, expiresAt, normalizedPhone);
    }

    public OtpVerificationResult verifyOtpForPhone(String phone, String submittedOtp) {
        String normalizedPhone = normalizePhone(phone);

        if ("twilio".equalsIgnoreCase(appProperties.getSmsProvider())) {
            return verifyTwilioCode(normalizedPhone, submittedOtp);
        }

        OtpRecord record = otpRecords.get(normalizedPhone);

        if (record == null) {
            return new OtpVerificationResult(false, "No OTP request found for this number.", normalizedPhone);
        }

        synchronized (record) {
            if (System.currentTimeMillis() > record.getExpiresAt()) {
                otpRecords.remove(normalizedPhone);
                return new OtpVerificationResult(false, "OTP expired. Request a new code.", normalizedPhone);
            }

            if (!record.getCode().equals(submittedOtp)) {
                int failedAttempts = record.incrementFailedAttempts();
                if (failedAttempts >= MAX_OTP_ATTEMPTS) {
                    otpRecords.remove(normalizedPhone);
                    return new OtpVerificationResult(false, "Too many incorrect OTP attempts. Request a new code.", normalizedPhone);
                }
                return new OtpVerificationResult(false, "Incorrect OTP. Try again.", normalizedPhone);
            }
        }

        otpRecords.remove(normalizedPhone);
        return new OtpVerificationResult(true, "OTP verified.", normalizedPhone);
    }

    private void sendTwilioVerification(String normalizedPhone) {
        validateTwilioConfig();

        HttpHeaders headers = buildTwilioHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("To", normalizedPhone);
        body.add("Channel", "sms");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, headers);
        try {
            restTemplate.postForEntity(
                    appProperties.getTwilioVerifyBaseUrl() + "/Services/" + appProperties.getTwilioVerifyServiceSid() + "/Verifications",
                    request,
                    Map.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("SMS verification service is unavailable. Try again later.", exception);
        }
    }

    private OtpVerificationResult verifyTwilioCode(String normalizedPhone, String submittedOtp) {
        validateTwilioConfig();

        HttpHeaders headers = buildTwilioHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("To", normalizedPhone);
        body.add("Code", submittedOtp);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, headers);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(
                    appProperties.getTwilioVerifyBaseUrl() + "/Services/" + appProperties.getTwilioVerifyServiceSid() + "/VerificationCheck",
                    request,
                    Map.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("SMS verification service is unavailable. Try again later.", exception);
        }

        Map<?, ?> payload = response.getBody() != null ? response.getBody() : Collections.emptyMap();
        Object status = payload.get("status");

        if ("approved".equals(String.valueOf(status))) {
            return new OtpVerificationResult(true, "OTP verified.", normalizedPhone);
        }

        return new OtpVerificationResult(false, "Incorrect OTP. Try again.", normalizedPhone);
    }

    private HttpHeaders buildTwilioHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String credentials = appProperties.getTwilioAuthUser() + ":" + appProperties.getTwilioAuthPassword();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private void validateTwilioConfig() {
        if (isBlank(appProperties.getTwilioAuthUser())
                || isBlank(appProperties.getTwilioAuthPassword())
                || isBlank(appProperties.getTwilioVerifyServiceSid())) {
            throw new IllegalStateException("Twilio Verify is not configured. Set Twilio credentials and the Verify Service SID.");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        String trimmed = phone.trim().replaceAll("[\\s()-]", "");

        if (trimmed.startsWith("+")) {
            if (!trimmed.matches("^\\+[1-9]\\d{7,14}$")) {
                throw new IllegalArgumentException("Enter a valid phone number.");
            }
            return trimmed;
        }

        if (!trimmed.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Enter a valid 10-digit mobile number.");
        }

        String countryCode = appProperties.getDefaultCountryCode();

        if (isBlank(countryCode) || !countryCode.trim().matches("^\\+[1-9]\\d{0,3}$")) {
            throw new IllegalStateException("Default country code is not configured correctly.");
        }

        return countryCode.trim() + trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateOtp() {
        int minimum = (int) Math.pow(10, 5);
        int maximum = (int) Math.pow(10, 6) - 1;
        int value = minimum + random.nextInt(maximum - minimum + 1);
        return String.valueOf(value);
    }

    private static class OtpRecord {
        private final String code;
        private final long expiresAt;
        private int failedAttempts;

        private OtpRecord(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.failedAttempts = 0;
        }

        private String getCode() {
            return code;
        }

        private long getExpiresAt() {
            return expiresAt;
        }

        private int incrementFailedAttempts() {
            failedAttempts += 1;
            return failedAttempts;
        }
    }

    public static class OtpCreationResult {
        private final String code;
        private final long expiresAt;
        private final String normalizedPhone;

        public OtpCreationResult(String code, long expiresAt, String normalizedPhone) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.normalizedPhone = normalizedPhone;
        }

        public String getCode() {
            return code;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public String getNormalizedPhone() {
            return normalizedPhone;
        }

        public boolean isDevOtpAvailable() {
            return code != null;
        }
    }

    public static class OtpVerificationResult {
        private final boolean ok;
        private final String message;
        private final String normalizedPhone;

        public OtpVerificationResult(boolean ok, String message, String normalizedPhone) {
            this.ok = ok;
            this.message = message;
            this.normalizedPhone = normalizedPhone;
        }

        public boolean isOk() {
            return ok;
        }

        public String getMessage() {
            return message;
        }

        public String getNormalizedPhone() {
            return normalizedPhone;
        }
    }
}
