package com.ajisegiri.google_calendar.api;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
@JsonClassDescription("Request body needed to create or update an event")
public class CalendarEventRequest {

    @ToolParam(description = "Summary or title of the meeting")
    @JsonProperty(required = true, value = "summary")
    @JsonPropertyDescription("Summary or title of the meeting")
    private String summary;

    @ToolParam(required = false, description = "Detailed description of the event")
    @JsonProperty(required = false, value = "description")
    @JsonPropertyDescription("Detailed description of the event")
    private String description;

    @ToolParam(required = false, description = "Location where the event will take place. Online as default")
    @JsonProperty(required = false, value = "location")
    @JsonPropertyDescription("Location where the event will take place. Online as default")
    private String location;

    @ToolParam(required = true, description = "Start date and time in ISO 8601 format (e.g., '2025-03-24T10:00:00')")
    @JsonProperty(required = true, value = "startDateTime")
    @JsonPropertyDescription("Start date and time in ISO 8601 format (e.g., '2025-03-24T10:00:00')")
    private String startDateTime;

    @ToolParam(required = false, description = "End date and time in ISO 8601 format (e.g., '2025-03-24T11:00:00')")
    @JsonProperty(required = false, value = "endDateTime")
    @JsonPropertyDescription("End date and time in ISO 8601 format (e.g., '2025-03-24T11:00:00')")
    private String endDateTime;

    @JsonProperty(required = true, value = "timeZoneInIANA")
    @ToolParam(required = true, description = "Must be valid IANA time zone. If it's a UTC/GMT offset (GMT+1, UTC-5) or another non-IANA format, return the best-matching IANA identifier based on the user's region. Prioritize accuracy, considering daylight saving time (DST) where applicable. If the input is already in IANA format, return it unchanged.")
    @JsonPropertyDescription("Must be valid IANA time zone. If it's a UTC/GMT offset (GMT+1, UTC-5) or another non-IANA format, return the best-matching IANA identifier based on the user's region. Prioritize accuracy, considering daylight saving time (DST) where applicable. If the input is already in IANA format, return it unchanged.")
    private String timeZone;

    @ToolParam(required = false, description = "List of attendees' email addresses")
    @JsonProperty(required = false, value = "attendees")
    @JsonPropertyDescription("List of attendees' email addresses")
    private List<String> attendees;

    @ToolParam(required = true, description = "Indicates if the event is an online meeting. If true, a Google Meet link will be generated. If true, attendees field cannot be null or empty.")
    @JsonProperty(required = true, value = "isOnlineMeeting")
    @JsonPropertyDescription("Indicates if the event is an online meeting. If true, a Google Meet link will be generated. If true, attendees field cannot be null or empty.")
    private boolean isOnlineMeeting;
}
