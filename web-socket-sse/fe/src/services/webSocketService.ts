import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { GameEvent } from "../types/GameEvent";

// Backend URL for WebSocket connection
const SOCKET_URL = "http://localhost:8080/ws-game-events";

class WebSocketService {
  private stompClient: Stomp.Client | null = null;
  private connected = false;

  // Connect to the WebSocket server
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected) {
        resolve();
        return;
      }

      const socket = new SockJS(SOCKET_URL);
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect(
        {},
        () => {
          this.connected = true;
          console.log("WebSocket connection established");
          resolve();
        },
        (error) => {
          console.error("WebSocket connection error:", error);
          reject(error);
        }
      );
    });
  }

  // Disconnect from the WebSocket server
  disconnect(): void {
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect(() => {
        console.log("WebSocket disconnected");
        this.connected = false;
      });
    }
  }

  // Send a GameEvent to the server
  sendGameEvent(gameEvent: GameEvent): void {
    if (!this.stompClient || !this.connected) {
      console.error("Cannot send message, not connected");
      return;
    }

    // Set the timestamp to the current time if not provided
    const eventWithTimestamp: GameEvent = {
      ...gameEvent,
      timestamp: gameEvent.timestamp || new Date().toISOString(),
    };

    // Send the event to the recordEvent endpoint
    this.stompClient.send(
      "/app/record-event",
      {},
      JSON.stringify(eventWithTimestamp)
    );
  }
}

// Create a singleton instance
const webSocketService = new WebSocketService();
export default webSocketService;
