package com.ajisegiri.google_calendar.service;

import com.ajisegiri.google_calendar.api.CalendarEventRequest;
import com.ajisegiri.google_calendar.api.EventsResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleOAuthService googleOAuthService;
    private static String CALENDAR_ID = "f7861edb71e65be395745ca37215d3d77c941c74e54264e41188015e2573668b@group.calendar.google.com";
    // for personal calendar use "primary"

    public List<CalendarListEntry> getCalendarList(String userId) throws IOException {
        Calendar calendarService = getCalendarService(userId);
        return calendarService.calendarList().list().execute().getItems();
    }

    /**
     * Get events for the specified time period
     */
    public EventsResponse getEvents(String userId, LocalDateTime startDate, LocalDateTime endDate, String page, int pageSize) throws IOException {
        Calendar calendarService = googleOAuthService.getCalendarService(userId);
        DateTime start = new DateTime(startDate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        DateTime end = new DateTime(endDate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

        Events events = calendarService.events().list(CALENDAR_ID)
                .setTimeMin(start)
                .setTimeMax(end)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setMaxResults(pageSize) // Set page size
                .setPageToken(page) // Implement pagination
                .execute();
        return new EventsResponse(events.getItems(), events.getNextPageToken(), null);
    }

    /**
     * Search for events matching the query
     */
    public EventsResponse searchEvents(String userId, String query, LocalDateTime startDate, LocalDateTime endDate, String page, int pageSize) throws Exception {
        // Use provided dates or default to current week
        DateTime start = startDate != null ?
                new DateTime(startDate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()) :
                new DateTime(getStartOfWeek());

        DateTime end = endDate != null ?
                new DateTime(endDate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()) :
                new DateTime(getEndOfWeek());

        Events events = getCalendarService(userId).events().list(CALENDAR_ID)
                .setQ(query) // Search by title, description, location, attendees
                .setTimeMin(start)
                .setTimeMax(end)
                .setMaxResults(pageSize)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return new EventsResponse(events.getItems(), events.getNextPageToken(), null);
    }

    /**
     * Create a new calendar event
     */
    public Event createEvent(String userId, CalendarEventRequest request) throws IOException {
        Event event = new Event()
                .setSummary(request.getSummary())
                .setLocation(request.getLocation())
                .setDescription(request.getDescription());

        // Set start and end time
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(request.getStartDateTime()))
                .setTimeZone(request.getTimeZone());
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(request.getEndDateTime()))
                .setTimeZone(request.getTimeZone());
        event.setEnd(end);

        // Add attendees if provided
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            List<EventAttendee> eventAttendees = request.getAttendees().stream()
                    .map(email -> new EventAttendee().setEmail(email))
                    .toList();
            event.setAttendees(eventAttendees);
        }

        // Conditionally add Google Meet link
        if (request.isOnlineMeeting()) {
            ConferenceData conferenceData = new ConferenceData();
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
            createConferenceRequest.setRequestId("meet-" + System.currentTimeMillis());
            ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
            conferenceSolutionKey.setType("hangoutsMeet");
            createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);
            conferenceData.setCreateRequest(createConferenceRequest);
            event.setConferenceData(conferenceData);
        }

        // Insert event into calendar
        event = getCalendarService(userId).events().insert(CALENDAR_ID, event)
                .setSendUpdates("all")
                .setConferenceDataVersion(1) // Required for Google Meet links
                .execute();
        return event;
    }

    /**
     * Update an existing calendar event
     */
    public Event updateEvent(String userId, String eventId, CalendarEventRequest request) throws IOException {
        Event existingEvent = getCalendarService(userId).events().get(CALENDAR_ID, eventId).execute();

        // Update fields based on the request
        if (request.getSummary() != null) {
            existingEvent.setSummary(request.getSummary());
        }

        if (request.getLocation() != null) {
            existingEvent.setLocation(request.getLocation());
        }

        if (request.getDescription() != null) {
            existingEvent.setDescription(request.getDescription());
        }

        // Update start and end time if provided
        if (request.getStartDateTime() != null) {
            EventDateTime start = existingEvent.getStart();
            if (start == null) {
                start = new EventDateTime();
            }
            start.setDateTime(new DateTime(request.getStartDateTime()))
                    .setTimeZone(request.getTimeZone());
            existingEvent.setStart(start);
        }

        if (request.getEndDateTime() != null) {
            EventDateTime end = existingEvent.getEnd();
            if (end == null) {
                end = new EventDateTime();
            }
            end.setDateTime(new DateTime(request.getEndDateTime()))
                    .setTimeZone(request.getTimeZone());
            existingEvent.setEnd(end);
        }

        // Update attendees if provided
        if (request.getAttendees() != null) {
            List<EventAttendee> eventAttendees = request.getAttendees().stream()
                    .map(email -> new EventAttendee().setEmail(email))
                    .toList();
            existingEvent.setAttendees(eventAttendees);
        }

        // Update online meeting status if changed
        boolean currentlyHasOnlineMeeting = existingEvent.getConferenceData() != null;

        if (request.isOnlineMeeting() && !currentlyHasOnlineMeeting) {
            // Add Google Meet link if it doesn't exist yet
            ConferenceData conferenceData = new ConferenceData();
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
            createConferenceRequest.setRequestId("meet-" + System.currentTimeMillis());
            ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
            conferenceSolutionKey.setType("hangoutsMeet");
            createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);
            conferenceData.setCreateRequest(createConferenceRequest);
            existingEvent.setConferenceData(conferenceData);
        } else if (!request.isOnlineMeeting() && currentlyHasOnlineMeeting) {
            // Remove Google Meet link
            existingEvent.setConferenceData(null);
        }

        // Update the event
        Event updatedEvent = getCalendarService(userId).events().update(CALENDAR_ID, eventId, existingEvent)
                .setSendUpdates("all")  // Send notifications to all attendees
                .setConferenceDataVersion(1)  // Required for Google Meet links
                .execute();

        return updatedEvent;
    }

    /**
     * Delete an event
     */
    public void deleteEvent(String userId, String eventId) throws IOException {
        Calendar calendarService = getCalendarService(userId);
        calendarService.events().delete(CALENDAR_ID, eventId).execute();
    }

    private Calendar getCalendarService(String userId) throws IOException {
        return googleOAuthService.getCalendarService(userId);
    }

    private Date getStartOfWeek() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        return Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date getEndOfWeek() {
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        return Date.from(endOfWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }
}