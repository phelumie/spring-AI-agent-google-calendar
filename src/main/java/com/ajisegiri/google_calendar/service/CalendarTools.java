package com.ajisegiri.google_calendar.service;

import com.ajisegiri.google_calendar.api.CalendarEventRequest;
import com.ajisegiri.google_calendar.api.EventsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarTools {

    private final GoogleCalendarService googleCalendarService;

    @Tool(description = "This retrieves a calendar events for a specific user. If startDate and endDate are not provided, set them to the current week.Dates are of JAVA LocalDateTime datatype.")
    public EventsResponse getCalendarEvent(String userId, String startDate, String endDate,
                                           @ToolParam(required = false, description = "This is nextPageToken,It can be null") String page,
                                           @ToolParam(required = false, description = "The size of event users wants to see.use default 100") Integer pageSize,
                                           ToolContext toolContext) {
        log.info("Getting calendar events - startDate: {}, endDate: {}, page: {}, pageSize: {}",
                startDate, endDate, page, pageSize != null ? pageSize : 1000);

        if (pageSize == null) {
            pageSize = 1000;
            log.debug("Using default pageSize: {}", pageSize);
        }

        try {
            log.debug("Fetching events with pageSize: {}", pageSize);
            var events = googleCalendarService.getEvents(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), page, pageSize);
            log.info("Successfully retrieved {} events", events.getEvents() != null ? events.getEvents().size() : 0);
            return events;
        } catch (Exception e) {
            String errorMessage = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            log.error("Calendar error while retrieving events: {}", errorMessage, e);
            return new EventsResponse(null, null, errorMessage);
        }
    }

    @Tool(description = "This searches calendar events pertaining to a user. If startDate and endDate are not provided, The code uses the current week.Dates are of JAVA LocalDateTime datatype." +
            "Must be only used when the intention of the user is to look up a specific word or phrase.")
    public EventsResponse searchEvents(String userId, String query, @ToolParam(required = false) String startDate, @ToolParam(required = false) String endDate,
                                       @ToolParam(required = false, description = "This is nextPageToken,It can be null") String page,
                                       @ToolParam(required = false, description = "The size of event users wants to see. use default as 100") Integer pageSize) {
        log.info("Searching calendar events - query: '{}', startDate: {}, endDate: {}, page: {}, pageSize: {}",
                query, startDate, endDate, page, pageSize != null ? pageSize : 1000);

        try {
            if (pageSize == null) {
                pageSize = 1000;
                log.debug("Using default pageSize: {}", pageSize);
            }

            var searchResults = googleCalendarService.searchEvents(userId, query, startDate == null ? null : LocalDateTime.parse(startDate),
                    endDate == null ? null : LocalDateTime.parse(endDate), page, pageSize);
            log.info("Search completed - found {} events matching query: '{}'",
                    searchResults.getEvents() != null ? searchResults.getEvents().size() : 0, query);
            return searchResults;
        } catch (Exception e) {
            String errorMessage = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            log.error("Calendar error while searching events: {}", errorMessage, e);
            return new EventsResponse(null, null, errorMessage);
        }
    }

    @Tool(description = "This create/schedule calendar event.")
    public String createEvent(String userId, @ToolParam(description = "Request body needed to create an event") CalendarEventRequest request,
                              @ToolParam(description = "The IANA time zone in which time is specified", required = false) String timeZoneInIANA) {
        log.info("Creating calendar event - summary: {}", request.getSummary());
        request.setTimeZone(timeZoneInIANA);

        try {
            log.debug("Event creation request details: {}", request);
            String result = googleCalendarService.createEvent(userId, request).toString();
            log.info("Event created successfully");
            return result;
        } catch (Exception e) {
            String errorMessage = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            log.error("Calendar error while creating event: {}", errorMessage, e);
            return "Error details: " + errorMessage;
        }
    }

    @Tool(description = "This updates an existing calendar event.Note ID not htmlLink eid")
    public String updateEvent(String userId, String eventId, @ToolParam(description = "Request body needed to update an event. Not Every field has to be field") CalendarEventRequest request
            , @ToolParam(description = "The IANA time zone in which time is specified", required = false) String timeZoneInIANA) {
        log.info("Updating calendar event - eventId: {}, summary: {}", eventId, request.getSummary());
        request.setTimeZone(timeZoneInIANA);

        try {
            log.debug("Event update details - eventId: {}, request: {}", eventId, request);
            String result = googleCalendarService.updateEvent(userId, eventId, request).toString();
            log.info("Event updated successfully - eventId: {}", eventId);
            return result;
        } catch (Exception e) {
            String errorMessage = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            log.error("Calendar error while updating event {}: {}", eventId, errorMessage, e);
            return "Error details: " + errorMessage;
        }
    }

    @Tool(description = "delete a specific meeting/event. Use the value of the id field. Do not extract eid or any other field. use only the id value.")
    public String deleteEvent(String userId, @ToolParam(description = "Meeting event ID not name") String eventId) {
        log.info("Deleting calendar event - eventId: {}", eventId);

        try {
            googleCalendarService.deleteEvent(userId, eventId);
            log.info("Event deleted successfully - eventId: {}", eventId);
            return "Successfully deleted";
        } catch (Exception e) {
            String errorMessage = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            log.error("Calendar error while deleting event {}: {}", eventId, errorMessage, e);
            return "Error details: " + errorMessage;
        }
    }
}