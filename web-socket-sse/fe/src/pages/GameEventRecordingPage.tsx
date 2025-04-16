import { GameEventForm } from "../components/GameEventForm";
import "./GameEventRecordingPage.css";

export const GameEventRecordingPage = () => {
  return (
    <div className="game-event-recording-page">
      <header>
        <h1>Game Event Recorder</h1>
        <p>Record game events using WebSocket communication</p>
      </header>
      <main>
        <GameEventForm />
      </main>
      <footer>
        <p>Game Events Recorder - WebSocket Demo</p>
      </footer>
    </div>
  );
};
