import { useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { useGameEventStore } from "../store/gameEventStore";
import webSocketService from "../services/webSocketService";
import { GameEvent } from "../types/GameEvent";

export const useWebSocketConnection = () => {
  const {
    connectionStatus,
    setConnectionStatus,
    setIsSubmitting,
    setSubmitted,
    setError,
    resetForm,
    setEvents,
  } = useGameEventStore();

  // Connect to WebSocket when component mounts
  useEffect(() => {
    let isSubscribed = true;

    const connectToWebSocket = async () => {
      try {
        setConnectionStatus("connecting");
        await webSocketService.connect();

        // Only proceed if the component is still mounted
        if (isSubscribed) {
          setConnectionStatus("connected");

          // Subscribe to game events after connection is established
          setTimeout(() => {
            if (isSubscribed) {
              webSocketService.subscribeToGameEvents((events) => {
                console.log("Received game events:", events);
                setEvents(events);
              });
            }
          }, 100); // Small delay to ensure connection is ready
        }
      } catch (error) {
        console.error("Failed to connect to WebSocket:", error);
        if (isSubscribed) {
          setConnectionStatus("disconnected");
          setError("Connection failed. Please try again.");
        }
      }
    };

    connectToWebSocket();

    // Cleanup function
    return () => {
      isSubscribed = false;
      webSocketService.disconnect();
      setConnectionStatus("disconnected");
    };
  }, [setConnectionStatus, setError, setEvents]);

  // Mutation for sending game events
  const sendGameEventMutation = useMutation({
    mutationFn: (gameEvent: GameEvent) => {
      return new Promise<void>((resolve, reject) => {
        try {
          if (connectionStatus !== "connected") {
            reject(new Error("Not connected to WebSocket server"));
            return;
          }

          webSocketService.sendGameEvent(gameEvent);
          resolve();
        } catch (error) {
          reject(error);
        }
      });
    },
    onMutate: () => {
      setIsSubmitting(true);
      setError(null);
    },
    onSuccess: () => {
      setIsSubmitting(false);
      setSubmitted(true);
      setTimeout(() => {
        resetForm();
      }, 2000);
    },
    onError: (error: Error) => {
      setIsSubmitting(false);
      setError(error.message || "Failed to send event");
    },
  });

  return {
    connectionStatus,
    sendGameEvent: sendGameEventMutation.mutate,
    isSubmitting: sendGameEventMutation.isPending,
    error: sendGameEventMutation.error?.message || null,
  };
};
