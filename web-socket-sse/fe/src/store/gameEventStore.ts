import { create } from "zustand";
import { GameEvent, createEmptyGameEvent } from "../types/GameEvent";

interface GameEventState {
  // Form state
  currentEvent: GameEvent;
  isSubmitting: boolean;
  submitted: boolean;
  error: string | null;
  connectionStatus: "disconnected" | "connecting" | "connected";

  // Events history
  events: GameEvent[];

  // Actions
  setCurrentEvent: (event: GameEvent) => void;
  updateEventField: <K extends keyof GameEvent>(
    field: K,
    value: GameEvent[K]
  ) => void;
  setIsSubmitting: (isSubmitting: boolean) => void;
  setSubmitted: (submitted: boolean) => void;
  setError: (error: string | null) => void;
  resetForm: () => void;
  setConnectionStatus: (
    status: "disconnected" | "connecting" | "connected"
  ) => void;

  // Event history actions
  setEvents: (events: GameEvent[]) => void;
  addEvent: (event: GameEvent) => void;
}

export const useGameEventStore = create<GameEventState>((set) => ({
  // Initial state
  currentEvent: createEmptyGameEvent(),
  isSubmitting: false,
  submitted: false,
  error: null,
  connectionStatus: "disconnected",
  events: [],

  // Actions
  setCurrentEvent: (event) => set({ currentEvent: event }),

  updateEventField: (field, value) =>
    set((state) => ({
      currentEvent: {
        ...state.currentEvent,
        [field]: value,
      },
    })),

  setIsSubmitting: (isSubmitting) => set({ isSubmitting }),

  setSubmitted: (submitted) => set({ submitted }),

  setError: (error) => set({ error }),

  resetForm: () =>
    set({
      currentEvent: createEmptyGameEvent(),
      isSubmitting: false,
      submitted: false,
      error: null,
    }),

  setConnectionStatus: (status) => set({ connectionStatus: status }),

  // Event history actions
  setEvents: (events) => set({ events }),

  addEvent: (event) =>
    set((state) => ({
      events: [...state.events, event],
    })),
}));
