import { useGameEventStore } from "../store/gameEventStore";
import { useWebSocketConnection } from "../hooks/useWebSocketConnection";
import { EventType } from "../types/GameEvent";
import "./GameEventForm.css";

export const GameEventForm = () => {
  const { currentEvent, updateEventField, submitted, error } =
    useGameEventStore();

  const { connectionStatus, sendGameEvent, isSubmitting } =
    useWebSocketConnection();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    sendGameEvent(currentEvent);
  };

  const isConnected = connectionStatus === "connected";
  const isConnecting = connectionStatus === "connecting";

  return (
    <div className="game-event-form">
      <h2>Record Game Event</h2>

      <div className="connection-status">
        Connection status:
        <span className={`status-${connectionStatus}`}>{connectionStatus}</span>
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
              <label htmlFor="successful">
                <input
                  id="successful"
                  type="checkbox"
                  checked={currentEvent.successful}
                  onChange={(e) =>
                    updateEventField("successful", e.target.checked)
                  }
                  disabled={isSubmitting || !isConnected}
                />
                Successful Shot
              </label>
            </div>

            <div className="form-group">
              <label htmlFor="points">Points:</label>
              <select
                id="points"
                value={currentEvent.points}
                onChange={(e) =>
                  updateEventField("points", parseInt(e.target.value))
                }
                disabled={isSubmitting || !isConnected}
              >
                <option value={1}>1 point</option>
                <option value={2}>2 points</option>
                <option value={3}>3 points</option>
              </select>
            </div>
          </>
        )}

        <button
          type="submit"
          disabled={isSubmitting || isConnecting || !isConnected}
        >
          {isSubmitting ? "Recording..." : "Record Event"}
        </button>
      </form>
    </div>
  );
};
