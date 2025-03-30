package com.ajisegiri.google_calendar.controller;

import com.ajisegiri.google_calendar.service.GoogleOAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.ajisegiri.google_calendar.controller.GoogleCalendarController.USER_ID;

@Slf4j
@RestController
public class OAuthCallbackController {

    private final GoogleOAuthService googleOAuthService;

    @Autowired
    public OAuthCallbackController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @GetMapping("/oauth2/auth")
    public ResponseEntity<String> getAuthUrl() {
        String authUrl = googleOAuthService.getAuthUrl(USER_ID);
        return ResponseEntity.ok(authUrl);
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam String code,
            @RequestParam String state) throws IOException {

        // The state parameter contains the userId
        String userId = state;
        log.info("Received code {} for user {}", code, userId);

        googleOAuthService.exchangeCodeForTokens(userId, code);
        return ResponseEntity.ok("Authorization successful!");
    }
}