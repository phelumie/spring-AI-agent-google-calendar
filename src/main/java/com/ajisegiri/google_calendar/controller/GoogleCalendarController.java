package com.ajisegiri.google_calendar.controller;

import com.ajisegiri.google_calendar.api.CalendarEventRequest;
import com.ajisegiri.google_calendar.api.EventsResponse;
import com.ajisegiri.google_calendar.service.GoogleCalendarService;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    public static final String USER_ID = UUID.randomUUID().toString(); // in prod this wil be Actual ID of the user

    @GetMapping("/list")
    public List<CalendarListEntry> calendarList() throws IOException {
        return googleCalendarService.getCalendarList(USER_ID);
    }

    @GetMapping("/events")
    public EventsResponse getEvents(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) throws Exception {
        // Default to current week if no dates are provided
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.with(DayOfWeek.MONDAY).atStartOfDay();  // Start of week (Monday)
            endDate = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);  // End of week (Sunday)
        }

        return googleCalendarService.getEvents(USER_ID, startDate, endDate, page, pageSize);
    }

    @GetMapping("/events/search")
    public EventsResponse search(
            @RequestParam("query") String query,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) throws Exception {
        return googleCalendarService.searchEvents(USER_ID, query, startDate, endDate, page, pageSize);
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createCalendarEvent(@RequestBody CalendarEventRequest request) throws Exception {
        var event = googleCalendarService.createEvent(USER_ID, request);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    // 5️⃣ Delete an Event
    @DeleteMapping("/events/{eventId}")
    public String deleteEvent(@PathVariable String eventId) throws Exception {
        googleCalendarService.deleteEvent(USER_ID, eventId);
        return "Event Deleted: " + eventId;
    }
}
