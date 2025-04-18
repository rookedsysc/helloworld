import { useState, useEffect, useCallback } from "react";
import webSocketService from "../services/webSocketService";
import { GameEvent } from "../types/GameEvent";

export const useWebSocketConnection = () => {
  const [isConnected, setIsConnected] = useState(false);

  const sendGameEvent = useCallback(
    async (event: GameEvent) => {
      if (!isConnected) {
        throw new Error("WebSocket is not connected");
      }
      await webSocketService.sendGameEvent(event);
    },
    [isConnected]
  );

  useEffect(() => {
    const connectToWebSocket = async () => {
      try {
        await webSocketService.connect();
        setIsConnected(true);
      } catch (error) {
        console.error("Failed to connect:", error);
        setIsConnected(false);
      }
    };

    connectToWebSocket();

    return () => {
      webSocketService.disconnect();
      setIsConnected(false);
    };
  }, []);

  return { isConnected, sendGameEvent };
};
