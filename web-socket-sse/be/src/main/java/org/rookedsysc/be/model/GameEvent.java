package org.rookedsysc.be.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    public enum EventType {
        SHOT, FOUL, EJECTION
    }

    private EventType type;
    private String playerId;
    private String playerName;
    private String teamId;
    private String teamName;
    private LocalDateTime timestamp;
    private String description;
    private Boolean successful; // For shots - whether it went in
    private Integer points; // For shots - number of points
} 