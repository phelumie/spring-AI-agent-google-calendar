# Spring AI Agent for Google Calendar

A sophisticated AI agent built with Spring Boot that interacts with Google Calendar using natural language processing.
This project demonstrates how to leverage OpenAI's language models to create, update, and manage calendar events through
conversational interfaces.

> [**Read the full tutorial: Building an AI Agent to Manage Google Calendar with Spring AI**](https://dev.to/sunday_ajisegiri/building-an-ai-agent-to-manage-google-calendar-with-spring-ai-180l) — includes a working demo of the application!

## 🌟 Features

- **OAuth 2.0 Authentication** - Seamless Google authentication with token refresh
- **AI-Powered Calendar Management** - Create, update, and delete events using natural language
- **Smart Event Creation** - AI extracts dates, times, durations, attendees, and locations from user queries
- **Conflict Resolution** - Suggests available time slots and resolves scheduling conflicts
- **Conversation Memory** - Maintains context across multiple interactions
- **Streaming Responses** - Real-time streaming of AI responses for better UX
- **Google Meet Integration** - Automatically creates meeting links for events
- **Time Zone Support** - Handles time zone conversions appropriately

## 📋 Requirements

- Java 21+
- Spring Boot 3.x
- Google Cloud Platform account
- OpenAI API key

## 🚀 Getting Started

### 1. Google Cloud Setup

1. Create a project in Google Cloud Console
2. Enable the Google Calendar API
3. Create OAuth 2.0 credentials (Web application type)
4. Set your redirect URL to `http://localhost:8080/oauth2/callback`
5. Add test users in the OAuth consent screen

### 2. Configuration

1. Clone the repository:
   ```bash
   https://github.com/phelumie/spring-AI-agent-google-calendar.git
   ```

2. Add your Google OAuth credentials:
    - Download the credentials JSON file from Google Cloud Console
    - Place it in `src/main/resources/`
    - Update the `CREDENTIALS_FILE_PATH` in `GoogleOAuthConfig` class

3. Configure your application properties:
   ```properties
   # src/main/resources/application.properties
   
   # OpenAI Configuration
   spring.ai.openai.api-key=your_openai_api_key
   
   # OpenAI Configuration only - Google credentials are configured in GoogleOAuthConfig
   ```

## 🏃‍♀️ Running the Application

1. Build the project:
   ```bash
   ./mvnw clean package
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Initialize OAuth authentication:
    - Navigate to `http://localhost:8080/oauth2/auth`
    - Follow the authorization flow using your test user account
    - Grant the requested permissions

4. Access the chatbot interface:
    - Navigate to `http://localhost:8080`
    - Start interacting with your calendar through natural language!

## 🧪 Calendar ID Configuration

```java
// For a specific calendar
private static String CALENDAR_ID = "f7861edb71e65be395745ca37215d3d77c941c74e54264e41188015e2573668b@group.calendar.google.com";

// For personal calendar use "primary" in **GoogleCalendarService**
// private static String CALENDAR_ID = "primary";
```

## 🤖 Example Interactions

- "Schedule a meeting with John and Mary tomorrow at 2 PM for 1 hour", add email address to send a notification
- "What meetings do I have this week?"
- "Reschedule my 3 PM meeting to 4 PM"
- "Cancel my meeting with the marketing team"
- "Show me my availability next Monday"

## 🧠 How It Works

The application uses Spring AI's function calling capabilities to bridge natural language understanding with Google
Calendar API operations. Key components include:

- **GoogleOAuthConfig**: Handles authentication and credential management
- **CalendarTools**: Provides calendar operations as tools for the AI
- **PromptChatMemoryAdvisor**: Maintains conversation context for better interactions

The system processes user queries through these steps:

1. Natural language input is processed by the AI model
2. The model identifies intent and extracts relevant parameters
3. Appropriate calendar tools are invoked based on the detected intent
4. Results are returned and presented to the user in natural language

## 📚 Architecture

```
src/
├── main/
│   ├── java/com/yourpackage/
│   │   ├── config/
│   │   │   ├── GoogleOAuthConfig.java
│   │   │   └── OpenAIConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ChatController.java
│   │   │   └── WebController.java
│   │   ├── model/
│   │   │   └── EventsResponse.java
│   │   ├── service/
│   │   │   ├── GoogleCalendarService.java
│   │   │   ├── GoogleOAuthService.java
│   │   │   └── ChatService.java
│   │   ├── tools/
│   │   │   └── CalendarTools.java
│   │   └── CalendarApplication.java
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.properties
└── test/
    └── java/
```

## 🔒 Security Considerations

- OAuth tokens are securely managed and refreshed
- In production environments, implement a proper credential storage solution (not in-memory)
- Implement proper error handling and logging

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 🔗 Links

- [Spring Boot Banking API](https://github.com/phelumie/Spring-boot-Banking-API) - Another project by the author
- [Google Calendar API Documentation](https://developers.google.com/calendar/api/guides/overview)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/index.html)

---

Built with ❤️ by [SUNDAY AJISEGIRI](https://github.com/phelumie)