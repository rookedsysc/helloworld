package org.rookedsysc.be.controller;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.be.model.GameEvent;
import org.rookedsysc.be.service.GameEventService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameEventController {

    private final GameEventService gameEventService;

    @MessageMapping("/record-event")
    public void recordEvent(GameEvent event) {
        gameEventService.recordEvent(event);
    }
} 