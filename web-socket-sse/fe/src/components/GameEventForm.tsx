import { useWebSocketConnection } from "../hooks/useWebSocketConnection";
import { EventType, GameEvent } from "../types/GameEvent";
import { useState } from "react";
import "./GameEventForm.css";

const initialEvent: GameEvent = {
  type: EventType.SHOT,
  playerId: "",
  playerName: "",
  teamId: "",
  teamName: "",
  timestamp: new Date().toISOString(),
  description: "",
  successful: false,
  points: 0,
};

export const GameEventForm = () => {
  const [currentEvent, setCurrentEvent] = useState<GameEvent>(initialEvent);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { isConnected, sendGameEvent } = useWebSocketConnection();

  const updateEventField = <K extends keyof GameEvent>(
    field: K,
    value: GameEvent[K]
  ) => {
    setCurrentEvent((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    setSubmitted(false);

    try {
      await sendGameEvent(currentEvent);
      setSubmitted(true);
      setCurrentEvent(initialEvent);
    } catch (error) {
      setError(error instanceof Error ? error.message : "Failed to send event");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="game-event-form">
      <h2>Record Game Event</h2>

      <div className="connection-status">
        Connection status:
        <span
          className={`status-${isConnected ? "connected" : "disconnected"}`}
        >
          {isConnected ? "connected" : "disconnected"}
        </span>
      </div>

      {submitted && (
        <div className="success-message">Event recorded successfully!</div>
      )}

      {error && <div className="error-message">Error: {error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="event-type">Event Type:</label>
          <select
            id="event-type"
            value={currentEvent.type}
            onChange={(e) =>
              updateEventField("type", e.target.value as EventType)
            }
            disabled={isSubmitting || !isConnected}
          >
            <option value={EventType.SHOT}>Shot</option>
            <option value={EventType.FOUL}>Foul</option>
            <option value={EventType.EJECTION}>Ejection</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="player-id">Player ID:</label>
          <input
            id="player-id"
            type="text"
            value={currentEvent.playerId}
            onChange={(e) => updateEventField("playerId", e.target.value)}
            disabled={isSubmitting || !isConnected}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="player-name">Player Name:</label>
          <input
            id="player-name"
            type="text"
            value={currentEvent.playerName}
            onChange={(e) => updateEventField("playerName", e.target.value)}
            disabled={isSubmitting || !isConnected}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="team-id">Team ID:</label>
          <input
            id="team-id"
            type="text"
            value={currentEvent.teamId}
            onChange={(e) => updateEventField("teamId", e.target.value)}
            disabled={isSubmitting || !isConnected}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="team-name">Team Name:</label>
          <input
            id="team-name"
            type="text"
            value={currentEvent.teamName}
            onChange={(e) => updateEventField("teamName", e.target.value)}
            disabled={isSubmitting || !isConnected}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description:</label>
          <textarea
            id="description"
            value={currentEvent.description}
            onChange={(e) => updateEventField("description", e.target.value)}
            disabled={isSubmitting || !isConnected}
            required
          />
        </div>

        {currentEvent.type === EventType.SHOT && (
          <>
            <div className="form-group">
              <label htmlFor="points">Points:</label>
              <input
                id="points"
                type="number"
                value={currentEvent.points}
                onChange={(e) =>
                  updateEventField("points", parseInt(e.target.value))
                }
                disabled={isSubmitting || !isConnected}
                required
                min="1"
                max="3"
              />
            </div>

            <div className="form-group">
              <label htmlFor="successful">Successful:</label>
              <input
                id="successful"
                type="checkbox"
                checked={currentEvent.successful}
                onChange={(e) =>
                  updateEventField("successful", e.target.checked)
                }
                disabled={isSubmitting || !isConnected}
              />
            </div>
          </>
        )}

        <button
          type="submit"
          disabled={isSubmitting || !isConnected}
          className="submit-button"
        >
          {isSubmitting ? "Submitting..." : "Submit Event"}
        </button>
      </form>
    </div>
  );
};
