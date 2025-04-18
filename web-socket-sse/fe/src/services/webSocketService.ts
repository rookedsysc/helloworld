import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { GameEvent } from "../types/GameEvent";

// Backend URL for WebSocket connection
const SOCKET_URL = "http://localhost:8080/ws-game-events";

class WebSocketService {
  private stompClient: Stomp.Client | null = null;
  private connected = false;
  private gameEventsSubscription: Stomp.Subscription | null = null;
  private connectionPromise: Promise<void> | null = null;
  private connectionAttempts = 0;
  private readonly MAX_RECONNECT_ATTEMPTS = 3;

  // Check if WebSocket is connected
  isConnected(): boolean {
    return this.connected && this.stompClient !== null;
  }

  // Connect to the WebSocket server
  connect(): Promise<void> {
    if (this.connectionPromise) {
      console.log("Connection attempt already in progress");
      return this.connectionPromise;
    }

    this.connectionAttempts++;
    console.log(`Attempting to connect (attempt ${this.connectionAttempts})`);

    this.connectionPromise = new Promise((resolve, reject) => {
      // Cleanup any existing connection
      if (this.stompClient) {
        console.log("Cleaning up existing connection");
        this.disconnect();
      }

      try {
        console.log("Initializing SockJS connection to:", SOCKET_URL);
        const socket = new SockJS(SOCKET_URL);

        socket.onopen = () => {
          console.log("SockJS connection opened");
        };

        socket.onerror = (error) => {
          console.error("SockJS connection error:", error);
        };

        this.stompClient = Stomp.over(socket);

        // Configure debug logging
        this.stompClient.debug = (str) => {
          console.debug("[STOMP]", str);
        };

        console.log("Attempting STOMP connection...");
        this.stompClient.connect(
          {},
          () => {
            this.connected = true;
            this.connectionAttempts = 0;
            console.log("STOMP connection established successfully");
            resolve();
          },
          (error) => {
            this.connected = false;
            this.stompClient = null;
            this.connectionPromise = null;
            console.error("STOMP connection error:", error);

            if (this.connectionAttempts < this.MAX_RECONNECT_ATTEMPTS) {
              console.log("Will attempt to reconnect...");
              setTimeout(() => {
                this.connect().then(resolve).catch(reject);
              }, 2000);
            } else {
              console.error("Max reconnection attempts reached");
              reject(error);
            }
          }
        );
      } catch (error) {
        console.error("Error during connection setup:", error);
        this.connectionPromise = null;
        reject(error);
      }
    });

    return this.connectionPromise;
  }

  // Disconnect from the WebSocket server
  disconnect(): void {
    console.log("Disconnecting WebSocket...");
    if (this.stompClient && this.connected) {
      // Unsubscribe from game events
      if (this.gameEventsSubscription) {
        console.log("Unsubscribing from game events");
        this.gameEventsSubscription.unsubscribe();
        this.gameEventsSubscription = null;
      }

      this.stompClient.disconnect(() => {
        console.log("WebSocket disconnected successfully");
        this.connected = false;
        this.connectionPromise = null;
      });
    } else {
      console.log("No active connection to disconnect");
    }
  }

  // Send a GameEvent to the server
  async sendGameEvent(gameEvent: GameEvent): Promise<void> {
    console.log("Attempting to send game event:", gameEvent);

    try {
      await this.ensureConnection();

      if (!this.stompClient || !this.connected) {
        throw new Error("Cannot send message, not connected");
      }

      // Set the timestamp to the current time if not provided
      const eventWithTimestamp: GameEvent = {
        ...gameEvent,
        timestamp: gameEvent.timestamp || new Date().toISOString(),
      };

      console.log("Sending event to /app/game-events:", eventWithTimestamp);

      // Send the event to the game-events endpoint
      this.stompClient.send(
        "/app/game-events",
        {},
        JSON.stringify(eventWithTimestamp)
      );

      console.log("Game event sent successfully");
    } catch (error) {
      console.error("Error sending game event:", error);
      throw error;
    }
  }

  // Subscribe to game events from the server
  async subscribeToGameEvents(
    callback: (events: GameEvent[]) => void
  ): Promise<void> {
    console.log("Attempting to subscribe to game events");

    try {
      await this.ensureConnection();

      if (!this.stompClient || !this.connected) {
        throw new Error("Cannot subscribe, not connected");
      }

      // Unsubscribe if already subscribed
      if (this.gameEventsSubscription) {
        console.log("Unsubscribing from existing subscription");
        this.gameEventsSubscription.unsubscribe();
      }

      console.log("Subscribing to /topic/game-events");

      // Subscribe to the game events topic
      this.gameEventsSubscription = this.stompClient.subscribe(
        "/topic/game-events",
        (message) => {
          console.log("Received message on /topic/game-events:", message);
          try {
            const events = JSON.parse(message.body) as GameEvent[];
            console.log("Parsed game events:", events);
            callback(events);
          } catch (error) {
            console.error("Error parsing game events:", error);
          }
        }
      );

      console.log("Successfully subscribed to game events");
    } catch (error) {
      console.error("Error subscribing to game events:", error);
      throw error;
    }
  }

  private async ensureConnection(): Promise<void> {
    if (!this.isConnected()) {
      console.log("No active connection, attempting to connect");
      await this.connect();
    }
  }
}

// Create a singleton instance
const webSocketService = new WebSocketService();
export default webSocketService;
