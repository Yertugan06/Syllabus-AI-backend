// observer/WebSocketProgressObserver.java
package com.syllabusai.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketProgressObserver implements ProgressObserver {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Register a WebSocket session for progress updates
     */
    public void registerSession(String sessionId, WebSocketSession session) {
        activeSessions.put(sessionId, session);
        log.debug("Registered WebSocket session: {}", sessionId);
    }

    /**
     * Unregister a WebSocket session
     */
    public void unregisterSession(String sessionId) {
        activeSessions.remove(sessionId);
        log.debug("Unregistered WebSocket session: {}", sessionId);
    }

    @Override
    public void update(int progress, String message) {
        broadcastMessage(createProgressMessage(progress, message));
        log.debug("WebSocket progress update: {}% - {}", progress, message);
    }

    @Override
    public void onComplete(String result) {
        broadcastMessage(createCompletionMessage(result));
        log.info("WebSocket processing complete: {}", result);
    }

    @Override
    public void onError(String error) {
        broadcastMessage(createErrorMessage(error));
        log.error("WebSocket processing error: {}", error);
    }

    /**
     * Broadcast message to all active WebSocket sessions
     */
    private void broadcastMessage(String message) {
        activeSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } else {
                    // Clean up closed sessions
                    unregisterSession(session.getId());
                }
            } catch (IOException e) {
                log.warn("Failed to send WebSocket message to session {}: {}", session.getId(), e.getMessage());
                unregisterSession(session.getId());
            }
        });
    }

    private String createProgressMessage(int progress, String message) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "PROGRESS",
                    "timestamp", System.currentTimeMillis(),
                    "progress", progress,
                    "message", message,
                    "stage", determineProcessingStage(progress)
            ));
        } catch (Exception e) {
            return String.format(
                    "{\"type\": \"PROGRESS\", \"progress\": %d, \"message\": \"%s\", \"timestamp\": %d}",
                    progress, message, System.currentTimeMillis()
            );
        }
    }

    private String createCompletionMessage(String result) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "COMPLETE",
                    "timestamp", System.currentTimeMillis(),
                    "result", result,
                    "success", true
            ));
        } catch (Exception e) {
            return String.format(
                    "{\"type\": \"COMPLETE\", \"result\": \"%s\", \"timestamp\": %d, \"success\": true}",
                    result, System.currentTimeMillis()
            );
        }
    }

    private String createErrorMessage(String error) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "ERROR",
                    "timestamp", System.currentTimeMillis(),
                    "error", error,
                    "success", false,
                    "suggestion", generateErrorSuggestion(error)
            ));
        } catch (Exception e) {
            return String.format(
                    "{\"type\": \"ERROR\", \"error\": \"%s\", \"timestamp\": %d, \"success\": false}",
                    error, System.currentTimeMillis()
            );
        }
    }

    private String determineProcessingStage(int progress) {
        if (progress <= 20) return "VALIDATION";
        if (progress <= 40) return "EXTRACTION";
        if (progress <= 60) return "ANALYSIS";
        if (progress <= 80) return "ENHANCEMENT";
        return "FINALIZING";
    }

    private String generateErrorSuggestion(String error) {
        if (error.toLowerCase().contains("file") || error.toLowerCase().contains("pdf")) {
            return "Please ensure the file is a valid PDF and try again.";
        } else if (error.toLowerCase().contains("size") || error.toLowerCase().contains("large")) {
            return "File size may be too large. Please try a smaller file (max 50MB).";
        } else if (error.toLowerCase().contains("ai") || error.toLowerCase().contains("api")) {
            return "AI service temporarily unavailable. Please try again in a few moments.";
        } else {
            return "An unexpected error occurred. Please try again or contact support.";
        }
    }

    /**
     * Get active session count for monitoring
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Send custom notification to specific session
     */
    public void sendNotification(String sessionId, String type, String message) {
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String notification = objectMapper.writeValueAsString(Map.of(
                        "type", type,
                        "message", message,
                        "timestamp", System.currentTimeMillis()
                ));
                session.sendMessage(new TextMessage(notification));
            } catch (IOException e) {
                log.warn("Failed to send notification to session {}: {}", sessionId, e.getMessage());
            }
        }
    }
}