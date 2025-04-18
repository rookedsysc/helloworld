import React from "react";
import { useNavigate } from "react-router-dom";
import { GameEventsList } from "../components/GameEventsList";
import { useWebSocketConnection } from "../hooks/useWebSocketConnection";
import { useGameEventStore } from "../store/gameEventStore";
import "./GameEventHistory.css";

export const GameEventHistory: React.FC = () => {
  const navigate = useNavigate();
  const { connectionStatus } = useGameEventStore();

  // WebSocket 연결 설정
  useWebSocketConnection();

  return (
    <div className="game-event-history">
      <header>
        <button className="back-button" onClick={() => navigate("/")}>
          Back to Main
        </button>
        <h1>Game Event History</h1>
        <div className="connection-status">
          Status:{" "}
          <span className={`status-${connectionStatus}`}>
            {connectionStatus}
          </span>
        </div>
      </header>
      <main>
        <GameEventsList />
      </main>
    </div>
  );
};
