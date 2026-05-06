package com.jukebox.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String clientOrigin;
    private String googleClientId;
    private String jwtSecret;
    private long jwtTtlSeconds;
    private String adminSecret;
    private String smsProvider;
    private String defaultCountryCode;
    private boolean exposeDevOtp;
    private String twilioAuthUser;
    private String twilioAuthPassword;
    private String twilioVerifyServiceSid;
    private String twilioVerifyBaseUrl = "https://verify.twilio.com/v2";

    public String getClientOrigin() {
        return clientOrigin;
    }

    public void setClientOrigin(String clientOrigin) {
        this.clientOrigin = clientOrigin;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtTtlSeconds() {
        return jwtTtlSeconds;
    }

    public void setJwtTtlSeconds(long jwtTtlSeconds) {
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    public String getAdminSecret() {
        return adminSecret;
    }

    public void setAdminSecret(String adminSecret) {
        this.adminSecret = adminSecret;
    }

    public String getSmsProvider() {
        return smsProvider;
    }

    public void setSmsProvider(String smsProvider) {
        this.smsProvider = smsProvider;
    }

    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }

    public void setDefaultCountryCode(String defaultCountryCode) {
        this.defaultCountryCode = defaultCountryCode;
    }

    public boolean isExposeDevOtp() {
        return exposeDevOtp;
    }

    public void setExposeDevOtp(boolean exposeDevOtp) {
        this.exposeDevOtp = exposeDevOtp;
    }

    public String getTwilioAuthUser() {
        return twilioAuthUser;
    }

    public void setTwilioAuthUser(String twilioAuthUser) {
        this.twilioAuthUser = twilioAuthUser;
    }

    public String getTwilioAuthPassword() {
        return twilioAuthPassword;
    }

    public void setTwilioAuthPassword(String twilioAuthPassword) {
        this.twilioAuthPassword = twilioAuthPassword;
    }

    public String getTwilioVerifyServiceSid() {
        return twilioVerifyServiceSid;
    }

    public void setTwilioVerifyServiceSid(String twilioVerifyServiceSid) {
        this.twilioVerifyServiceSid = twilioVerifyServiceSid;
    }

    public String getTwilioVerifyBaseUrl() {
        return twilioVerifyBaseUrl;
    }

    public void setTwilioVerifyBaseUrl(String twilioVerifyBaseUrl) {
        this.twilioVerifyBaseUrl = twilioVerifyBaseUrl;
    }
}
