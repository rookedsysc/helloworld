import React from "react";
import { useGameEventStore } from "../store/gameEventStore";
import { EventType } from "../types/GameEvent";
import "./GameEventsList.css";

export const GameEventsList: React.FC = () => {
  const { events } = useGameEventStore();

  // 게임 이벤트가 없는 경우
  if (events.length === 0) {
    return (
      <div className="game-events-list empty">
        <h2>Events History</h2>
        <p className="no-events">No events recorded yet.</p>
      </div>
    );
  }

  // 이벤트 타입에 따른 아이콘 및 클래스
  const getEventIcon = (type: EventType) => {
    switch (type) {
      case EventType.SHOT:
        return "🏀";
      case EventType.FOUL:
        return "🚫";
      case EventType.EJECTION:
        return "🔴";
      default:
        return "📝";
    }
  };

  const getEventClass = (type: EventType) => {
    return `event-type-${type.toLowerCase()}`;
  };

  // 타임스탬프 포맷팅
  const formatTimestamp = (timestamp: string | undefined) => {
    if (!timestamp) return "N/A";

    try {
      const date = new Date(timestamp);
      return date.toLocaleString();
    } catch {
      return timestamp;
    }
  };

  return (
    <div className="game-events-list">
      <h2>Events History</h2>
      <div className="events-container">
        {events.map((event, index) => (
          <div
            key={index}
            className={`event-item ${getEventClass(event.type)}`}
          >
            <div className="event-header">
              <span className="event-icon">{getEventIcon(event.type)}</span>
              <span className="event-type">{event.type}</span>
              <span className="event-time">
                {formatTimestamp(event.timestamp)}
              </span>
            </div>

            <div className="event-details">
              <div className="event-player">
                <strong>Player:</strong> {event.playerName} ({event.playerId})
              </div>
              <div className="event-team">
                <strong>Team:</strong> {event.teamName} ({event.teamId})
              </div>

              {event.type === EventType.SHOT && (
                <div className="event-shot-details">
                  <div>
                    <strong>Points:</strong> {event.points}
                  </div>
                  <div>
                    <strong>Successful:</strong>{" "}
                    {event.successful ? "Yes" : "No"}
                  </div>
                </div>
              )}

              <div className="event-description">
                <strong>Description:</strong> {event.description}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
