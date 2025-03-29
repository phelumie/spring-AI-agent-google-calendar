package com.ajisegiri.google_calendar.controller;

import com.ajisegiri.google_calendar.service.CalendarCustomerSupportAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.ajisegiri.google_calendar.controller.GoogleCalendarController.USER_ID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final CalendarCustomerSupportAssistantService agent;

    /**
     * Renders the chat UI template
     */
    @GetMapping("/")
    public String index() {
        return "chat";  // This maps to chat.html template
    }

    /**
     * API endpoint that streams the chat responses
     *
     * @param userMessage The message from the user
     * @return A streaming text response
     * @PathVariable chatId      A UUID that identifies the chat session
     */
    @PostMapping(value = "/chat/{chatId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> chat(@PathVariable String chatId, @RequestParam String userMessage) {
        return agent.chat(chatId, USER_ID, userMessage)
                .timeout(Duration.ofSeconds(30));
    }
}