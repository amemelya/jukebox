package com.jukebox.backend.exception;

import com.jukebox.backend.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void returnsServiceUnavailableForExternalProviderFailures() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse> response = handler.handleRestClientException(
                new RestClientException("twilio down")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(false, response.getBody().isOk());
        assertEquals(
                "External authentication service is unavailable. Try again later.",
                response.getBody().getMessage()
        );
    }
}
