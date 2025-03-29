/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ajisegiri.google_calendar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Service
@Slf4j
public class CalendarCustomerSupportAssistantService {

    private final ChatClient chatClient;

    public CalendarCustomerSupportAssistantService(ChatClient.Builder modelBuilder, ChatMemory chatMemory, CalendarTools calendarTools) {
        this.chatClient = modelBuilder
                .defaultSystem("""
                        You are an AI agent representing "REHOBOTH LABS" providing chat support to {user}.
                        Always respond in a personal, friendly, helpful, and joyful manner.
                        You are interacting with {user} (User ID: {user_id}) through an online chat system.
                        
                        Guidelines:
                        - Reference message history before asking the user for information they've already provided
                        - Be context-specific and follow up on previous messages appropriately
                        - Before changing any event, ensure it is permitted by the company terms
                        - Use the provided functions to fetch calendar events and cancel events
                        - Use the response data exactly as provided, even if there are duplicates event
                        - Use parallel function calling when appropriate for efficiency
                        - For calendar/event creation, always confirm the timezone before actually saving the event.
                        - Don't manipulate/change dates/calendar unless explicitly requested by the user
                        - Prioritize accuracy and precision in all interactions
                        - Always work with the user's timezone. Current timestamp is {current_date}
                        """)
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory), // Chat Memory
                        new LoggingAdvisor())

                .defaultTools(calendarTools) // FUNCTION CALLING
                .build();
    }

    public Flux<String> chat(String chatId, String userId, String userMessageContent) {
        log.info("Chat ID: " + chatId);
        log.info("User Message: " + userMessageContent);
        return this.chatClient.prompt()
                .system(s -> s.param("current_date", OffsetDateTime.now().toString()))
                .system(s -> s.param("user", "Sunday Ajisegiri"))
                .system(s -> s.param("user_id", userId))
                .user(userMessageContent)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream().content();
    }
}