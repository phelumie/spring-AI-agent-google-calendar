package com.ajisegiri.google_calendar.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class GoogleOAuthService {

    private static final String APPLICATION_NAME = "My Calendar App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;
    private final HttpTransport httpTransport;

    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";

    public GoogleOAuthService(GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow, HttpTransport httpTransport) {
        this.googleAuthorizationCodeFlow = googleAuthorizationCodeFlow;
        this.httpTransport = httpTransport;
    }

    // Generate Google OAuth URL
    public String getAuthUrl(String userId) {
        AuthorizationCodeRequestUrl authorizationUrl = googleAuthorizationCodeFlow.newAuthorizationUrl()
                .setState(userId)
                .setRedirectUri(REDIRECT_URI);
        return authorizationUrl.build();
    }

    // Exchange code for tokens and store them
    public void exchangeCodeForTokens(String userId, String code) throws IOException {
        log.info("Exchanging code for user {}", userId);
        TokenResponse tokenResponse = googleAuthorizationCodeFlow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URI)
                .execute();
        // Store the credential for this user
        googleAuthorizationCodeFlow.createAndStoreCredential(tokenResponse, userId);
    }

    // Exchange auth code for access & refresh tokens- THIS returns the access and refresh token
    public TokenResponse getTokenResponse(String code) throws Exception {
        return googleAuthorizationCodeFlow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URI)
                .execute();
    }

    public Credential refreshAccessToken(String accessToken, String refreshToken) throws Exception {
        var credential = createCredential(accessToken, refreshToken);
        boolean refreshed = credential.refreshToken();

        if (refreshed) {
            // Return the new access token
            return credential;
        } else {
            throw new IOException("Failed to refresh access token");
        }
    }

    // Get stored credentials for a user
    public Credential getStoredCredentials(String userId) throws IOException {
        Credential credential = googleAuthorizationCodeFlow.loadCredential(userId);
        if (credential != null && credential.getRefreshToken() != null) {
            // Refresh the token if it's expired or close to expiration
            Long expirationTime = credential.getExpirationTimeMilliseconds();
            if (expirationTime != null && expirationTime - System.currentTimeMillis() < 60000) {
                log.info("Refresh token expired for {} at {}", userId, expirationTime);
                boolean refreshed = credential.refreshToken();
                log.info("Refresh token refreshed {}", refreshed);
                if (!refreshed) {
                    throw new IOException("Failed to refresh access token");
                }
            }
            return credential;
        }
        return null;
    }

    private Credential createCredential(String accessToken, String refreshToken) {
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(googleAuthorizationCodeFlow.getTransport())
                .setJsonFactory(googleAuthorizationCodeFlow.getJsonFactory())
                .setClientAuthentication(googleAuthorizationCodeFlow.getClientAuthentication())
                .setTokenServerEncodedUrl(googleAuthorizationCodeFlow.getTokenServerEncodedUrl())
                .build();

        // Set the tokens
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);

        return credential;
    }

    // Get Calendar service using stored credentials
    public Calendar getCalendarService(String userId) throws IOException {
        Credential credential = getStoredCredentials(userId);
        if (credential == null) {
            throw new IOException("No stored credentials found for user: " + userId);
        }
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

//    public Calendar getCalendarService(String accessToken) {
//        // Create GoogleCredentials using the access token
//        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
//
//        // Use HttpCredentialsAdapter to adapt GoogleCredentials to HttpRequestInitializer
//        HttpCredentialsAdapter requestInitializer = new HttpCredentialsAdapter(credentials);
//
//        return new Calendar.Builder(httpTransport, JSON_FACTORY, requestInitializer)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }

    public List<Event> getEvents(String token) throws IOException {
        // Get events from primary calendar
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = getCalendarService(token).events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }
}
