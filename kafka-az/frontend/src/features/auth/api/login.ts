import { apiRequest } from "../../../shared/api";

export type LoginRequest = {
  id: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: "Bearer";
  isNewMember: boolean;
};

export function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });
}
