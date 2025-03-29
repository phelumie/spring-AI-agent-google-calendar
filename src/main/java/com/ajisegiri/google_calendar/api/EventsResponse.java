package com.ajisegiri.google_calendar.api;

import com.google.api.services.calendar.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventsResponse {
    private List<Event> events;
    private String nextPageToken;
    private String errorMessage;
}
