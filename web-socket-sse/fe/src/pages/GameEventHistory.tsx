import React from "react";
import { useNavigate } from "react-router-dom";
import "./GameEventHistory.css";

export const GameEventHistory: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="game-event-history">
      <header>
        <button className="back-button" onClick={() => navigate("/")}>
          Back to Main
        </button>
        <h1>Game Event History</h1>
      </header>
      <main>
        {/* Event history content will be added here */}
        <p>Event history coming soon...</p>
      </main>
    </div>
  );
};
