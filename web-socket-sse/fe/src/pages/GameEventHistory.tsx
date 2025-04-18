import React from "react";
import { useNavigate } from "react-router-dom";
import { GameEventsList } from "../components/GameEventsList";
import { useWebSocketConnection } from "../hooks/useWebSocketConnection";
import "./GameEventHistory.css";

export const GameEventHistory: React.FC = () => {
  const navigate = useNavigate();
  const { isConnected } = useWebSocketConnection();

  return (
    <div className="game-event-history">
      <header>
        <button className="back-button" onClick={() => navigate("/")}>
          Back to Main
        </button>
        <h1>Game Event History</h1>
        <div className="connection-status">
          Status:{" "}
          <span
            className={`status-${isConnected ? "connected" : "disconnected"}`}
          >
            {isConnected ? "Connected" : "Disconnected"}
          </span>
        </div>
      </header>
      <main>
        <GameEventsList />
      </main>
    </div>
  );
};
