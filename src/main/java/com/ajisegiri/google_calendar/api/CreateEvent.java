package com.ajisegiri.google_calendar.api;

import lombok.Data;

import java.util.Date;

@Data
public class CreateEvent {

    private String summary;
    private String description;
    private Date start;
    private Date end;
}
