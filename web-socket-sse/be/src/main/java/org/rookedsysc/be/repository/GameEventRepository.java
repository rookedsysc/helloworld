package org.rookedsysc.be.repository;

import org.rookedsysc.be.model.GameEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GameEventRepository {
    private static final List<GameEvent> gameEvents = new ArrayList<>();

    public List<GameEvent> findAll() {
        return gameEvents;
    }

    public GameEvent save(GameEvent gameEvent) {
        gameEvents.add(gameEvent);
        return gameEvent;
    }
}
