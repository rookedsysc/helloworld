import { useEffect, useCallback, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import webSocketService from "../services/webSocketService";
import { GameEvent } from "../types/GameEvent";
import { useGameEventStore } from "../store/gameEventStore";

export const useWebSocketConnection = () => {
  const [isConnecting, setIsConnecting] = useState(false);
  const setConnectionStatus = useGameEventStore(
    (state) => state.setConnectionStatus
  );
  const setEvents = useGameEventStore((state) => state.setEvents);

  const connectToWebSocket = useCallback(async () => {
    if (isConnecting || webSocketService.isConnected()) {
      return;
    }

    try {
      setIsConnecting(true);
      setConnectionStatus("connecting");

      await webSocketService.connect();

      // Only subscribe after successful connection
      webSocketService.subscribeToGameEvents((events: GameEvent[]) => {
        setEvents(events);
      });

      setConnectionStatus("connected");
    } catch (error) {
      console.error("Failed to connect to WebSocket:", error);
      setConnectionStatus("disconnected");
    } finally {
      setIsConnecting(false);
    }
  }, [setConnectionStatus, setEvents]);

  const sendGameEvent = useMutation({
    mutationFn: async (gameEvent: GameEvent) => {
      if (!webSocketService.isConnected()) {
        throw new Error("WebSocket is not connected");
      }
      await webSocketService.sendGameEvent(gameEvent);
    },
  });

  useEffect(() => {
    connectToWebSocket();

    return () => {
      webSocketService.disconnect();
    };
  }, [connectToWebSocket]);

  return {
    isConnected: webSocketService.isConnected(),
    isConnecting,
    sendGameEvent,
  };
};
