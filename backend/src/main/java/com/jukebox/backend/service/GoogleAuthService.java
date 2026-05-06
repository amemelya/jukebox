package com.jukebox.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.UserProfile;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

    private final AppProperties appProperties;

    public GoogleAuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public UserProfile verifyCredential(String credential) {
        if (isBlank(appProperties.getGoogleClientId())) {
            throw new IllegalStateException("Google sign-in is not configured. Set GOOGLE_CLIENT_ID first.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(appProperties.getGoogleClientId().trim()))
                    .build();
            GoogleIdToken idToken = verifier.verify(credential);

            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google credential.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return new UserProfile(
                    "google",
                    payload.getSubject(),
                    (String) payload.get("name"),
                    payload.getEmail(),
                    (String) payload.get("picture"),
                    null
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Google token verification failed.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not reach Google token verification service.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
