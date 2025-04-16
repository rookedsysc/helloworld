export enum EventType {
  SHOT = "SHOT",
  FOUL = "FOUL",
  EJECTION = "EJECTION",
}

export interface GameEvent {
  type: EventType;
  playerId: string;
  playerName: string;
  teamId: string;
  teamName: string;
  timestamp?: string; // using string for easier form handling, will convert to LocalDateTime
  description: string;
  successful?: boolean; // For shots - whether it went in
  points?: number; // For shots - number of points
}

export const createEmptyGameEvent = (): GameEvent => ({
  type: EventType.SHOT,
  playerId: "",
  playerName: "",
  teamId: "",
  teamName: "",
  description: "",
  successful: false,
  points: 0,
});
