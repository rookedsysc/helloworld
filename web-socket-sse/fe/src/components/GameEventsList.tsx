import React, { useEffect, useState } from "react";
import { EventType, GameEvent } from "../types/GameEvent";
import { useWebSocketConnection } from "../hooks/useWebSocketConnection";
import webSocketService from "../services/webSocketService";
import "./GameEventsList.css";

export const GameEventsList: React.FC = () => {
  const [events, setEvents] = useState<GameEvent[]>([]);
  const { isConnected } = useWebSocketConnection();

  useEffect(() => {
    const handleEvents = (newEvents: GameEvent[]) => {
      setEvents((prevEvents) => [...prevEvents, ...newEvents]);
    };

    if (isConnected) {
      webSocketService.subscribeToGameEvents(handleEvents).catch((error) => {
        console.error("Failed to subscribe to events:", error);
      });
    }

    return () => {
      setEvents([]);
    };
  }, [isConnected]);

  // 이벤트를 타임스탬프 기준으로 정렬
  const sortedEvents = [...events].sort((a, b) => {
    const timeA = a.timestamp ? new Date(a.timestamp).getTime() : 0;
    const timeB = b.timestamp ? new Date(b.timestamp).getTime() : 0;
    return timeB - timeA; // 최신 이벤트가 위에 오도록 정렬
  });

  // 게임 이벤트가 없는 경우
  if (sortedEvents.length === 0) {
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
      return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      }).format(date);
    } catch {
      return timestamp;
    }
  };

  // 이벤트 타입에 따른 배경색 설정
  const getEventBackgroundColor = (type: EventType) => {
    switch (type) {
      case EventType.SHOT:
        return "#e8f5e9";
      case EventType.FOUL:
        return "#fff3e0";
      case EventType.EJECTION:
        return "#ffebee";
      default:
        return "#f5f5f5";
    }
  };

  return (
    <div className="game-events-list">
      <h2>Events History</h2>
      <div className="events-container">
        {sortedEvents.map((event, index) => (
          <div
            key={`${event.timestamp}-${index}`}
            className={`event-item ${getEventClass(event.type)}`}
            style={{ backgroundColor: getEventBackgroundColor(event.type) }}
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
                    {event.successful ? "✅ Yes" : "❌ No"}
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
