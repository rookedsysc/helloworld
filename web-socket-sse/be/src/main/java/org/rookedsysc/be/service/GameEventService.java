package org.rookedsysc.be.service;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.be.model.GameEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;
    private final List<GameEvent> gameEvents = new ArrayList<>();

    /**
     * Record a new game event and broadcast it to all observers
     */
    public GameEvent recordEvent(GameEvent event) {
        // Set timestamp if not provided
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        // Add to history
        gameEvents.add(event);
        
        // Broadcast to all observers via WebSocket
        messagingTemplate.convertAndSend("/topic/game-events", event);
        
        return event;
    }
    
    /**
     * Get all events that have occurred
     */
    public List<GameEvent> getAllEvents() {
        return new ArrayList<>(gameEvents);
    }
} 