import { useEffect, useState } from "react";
import webSocketService from "../services/webSocketService";
import { GameEvent } from "../types/GameEvent";
import { useGameEventStore } from "../store/gameEventStore";

export const useWebSocketConnection = () => {
  const [isConnected, setIsConnected] = useState(false);
  const { setEvents, addEvent, setConnectionStatus } = useGameEventStore();

  useEffect(() => {
    const connectAndSubscribe = async () => {
      try {
        setConnectionStatus("connecting");
        await webSocketService.connect();
        setIsConnected(true);
        setConnectionStatus("connected");

        await webSocketService.subscribeToGameEvents((events: GameEvent[]) => {
          if (Array.isArray(events) && events.length > 1) {
            setEvents(events);
          } else if (events.length === 1) {
            addEvent(events[0]);
          }
        });
      } catch (error) {
        console.error("Failed to connect:", error);
        setConnectionStatus("disconnected");
        setIsConnected(false);
      }
    };

    connectAndSubscribe();

    return () => {
      webSocketService.disconnect();
      setConnectionStatus("disconnected");
      setIsConnected(false);
    };
  }, [setEvents, addEvent, setConnectionStatus]);

  const sendGameEvent = async (event: GameEvent) => {
    try {
      await webSocketService.sendGameEvent(event);
    } catch (error) {
      console.error("Failed to send game event:", error);
      throw error;
    }
  };

  return {
    isConnected,
    sendGameEvent,
  };
};
