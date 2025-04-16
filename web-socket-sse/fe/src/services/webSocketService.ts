import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { GameEvent } from "../types/GameEvent";

// Backend URL for WebSocket connection
const SOCKET_URL = "http://localhost:8080/ws-game-events";

class WebSocketService {
  private stompClient: Stomp.Client | null = null;
  private connected = false;
  private gameEventsSubscription: any = null;

  // Check if WebSocket is connected
  isConnected(): boolean {
    return this.connected && this.stompClient !== null;
  }

  // Connect to the WebSocket server
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnected()) {
        resolve();
        return;
      }

      // Cleanup any existing connection
      if (this.stompClient) {
        this.disconnect();
      }

      const socket = new SockJS(SOCKET_URL);
      this.stompClient = Stomp.over(socket);

      // Disable debug logging
      this.stompClient.debug = null;

      this.stompClient.connect(
        {},
        () => {
          this.connected = true;
          console.log("WebSocket connection established");
          resolve();
        },
        (error) => {
          this.connected = false;
          this.stompClient = null;
          console.error("WebSocket connection error:", error);
          reject(error);
        }
      );
    });
  }

  // Disconnect from the WebSocket server
  disconnect(): void {
    if (this.stompClient && this.connected) {
      // Unsubscribe from game events
      if (this.gameEventsSubscription) {
        this.gameEventsSubscription.unsubscribe();
        this.gameEventsSubscription = null;
      }

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

  // Subscribe to game events from the server
  subscribeToGameEvents(callback: (events: GameEvent[]) => void): void {
    if (!this.stompClient || !this.connected) {
      console.error("Cannot subscribe, not connected");
      return;
    }

    // Unsubscribe if already subscribed
    if (this.gameEventsSubscription) {
      this.gameEventsSubscription.unsubscribe();
    }

    // Subscribe to the game events topic
    this.gameEventsSubscription = this.stompClient.subscribe(
      "/topic/game-events",
      (message) => {
        try {
          const events = JSON.parse(message.body) as GameEvent[];
          callback(events);
        } catch (error) {
          console.error("Error parsing game events:", error);
        }
      }
    );

    console.log("Subscribed to game events");
  }
}

// Create a singleton instance
const webSocketService = new WebSocketService();
export default webSocketService;
