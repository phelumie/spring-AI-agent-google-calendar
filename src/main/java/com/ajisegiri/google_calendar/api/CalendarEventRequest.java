package com.ajisegiri.google_calendar.api;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class CalendarEventRequest {

    @ToolParam(required = true, description = "Summary or title of the meeting")
    private String summary;

    @ToolParam(required = false, description = "Detailed description of the event")
    private String description;

    @ToolParam(required = false, description = "Location where the event will take place. Online as default")
    private String location;

    @ToolParam(required = true, description = "Start date and time in ISO 8601 format (e.g., '2025-03-24T10:00:00')")
    private String startDateTime;

    @ToolParam(required = false, description = "End date and time in ISO 8601 format (e.g., '2025-03-24T11:00:00')")
    private String endDateTime;

    @ToolParam(required = true, description = "Time zone in IANA format (e.g., 'America/New_York')")
    private String timeZone;

    @ToolParam(required = false, description = "List of attendees' email addresses")
    private List<String> attendees;

    @ToolParam(required = true, description = "Indicates if the event is an online meeting. If true, a Google Meet link will be generated. If true, attendees field cannot be null or empty.")
    private boolean isOnlineMeeting;
}
