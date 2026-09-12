export interface AuthUser {
  id: string;
  name: string;
  email: string;
  role: string;
  treatmentsCompleted: number;
}

export interface AuthUserResponse {
  id: string;
  name: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUserResponse;
}

export interface RegisterResponse {
  email: string;
  emailSent: boolean;
  verifyUrl?: string | null;
}

export const AUTH_STORAGE_KEY = 'pgp-auth';
