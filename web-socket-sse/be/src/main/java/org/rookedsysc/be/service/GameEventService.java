package org.rookedsysc.be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rookedsysc.be.model.GameEvent;
import org.rookedsysc.be.repository.GameEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameEventRepository gameEventRepository;

    /**
     * Record a new game event and broadcast it to all observers
     */
    public void recordEvent(GameEvent event) {
        // Set timestamp if not provided
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        log.info("Recording event: {}", event.toString());

        // Add to history
        gameEventRepository.save(event);

        List<GameEvent> savedEvents = gameEventRepository.findAll();
        log.info("Send events: {}", savedEvents);

        // Broadcast to all observers via WebSocket
        messagingTemplate.convertAndSend("/topic/game-events", savedEvents);
    }
    
    /**
     * Get all events that have occurred
     */
    public List<GameEvent> getAllEvents() {
        return gameEventRepository.findAll();
    }
} 
