import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AUTH_STORAGE_KEY,
  AuthResponse,
  AuthUser,
  AuthUserResponse,
  RegisterResponse,
} from './auth.models';

interface StoredSession {
  accessToken: string;
  expiresAt: number;
  user: AuthUser;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly userSignal = signal<AuthUser | null>(null);
  private readonly tokenSignal = signal<string | null>(null);

  readonly user = this.userSignal.asReadonly();
  readonly token = this.tokenSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.userSignal() !== null && this.tokenSignal() !== null);

  constructor() {
    this.restore();
    queueMicrotask(() => this.refreshMe());
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/login`, { email, password })
      .pipe(tap((response) => this.persist(response)));
  }

  register(name: string, email: string, password: string): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${environment.apiUrl}/api/auth/register`, {
      name,
      email,
      password,
    });
  }

  verifyEmail(token: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/auth/verify-email`, { token });
  }

  resendVerification(email: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/auth/resend-verification`, { email });
  }

  logout(): void {
    this.clearSession();
  }

  clearSession(): void {
    this.userSignal.set(null);
    this.tokenSignal.set(null);
    try {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    } catch {
      /* ignore quota / private mode */
    }
  }

  completeTreatment(): void {
    const current = this.userSignal();
    if (!current) {
      return;
    }
    const next = { ...current, treatmentsCompleted: current.treatmentsCompleted + 1 };
    this.userSignal.set(next);
    this.writeStorage(this.tokenSignal(), next);
  }

  static errorKey(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'auth.genericError';
    }
    if (error.status === 0) {
      return 'auth.networkError';
    }
    const code = typeof error.error?.code === 'string' ? error.error.code : '';
    switch (code) {
      case 'EMAIL_TAKEN':
        return 'auth.emailTaken';
      case 'INVALID_CREDENTIALS':
        return 'auth.invalidCredentials';
      case 'EMAIL_NOT_VERIFIED':
        return 'auth.emailNotVerified';
      case 'INVALID_TOKEN':
        return 'auth.verifyInvalid';
      case 'ACCOUNT_DISABLED':
        return 'auth.accountDisabled';
      case 'TENANT_REQUIRED':
      case 'NOT_FOUND':
        return 'auth.tenantMissing';
      case 'VALIDATION_ERROR':
        return 'auth.validationError';
      default:
        return 'auth.genericError';
    }
  }

  private refreshMe(): void {
    if (!this.tokenSignal()) {
      return;
    }
    this.http.get<AuthUserResponse>(`${environment.apiUrl}/api/auth/me`).subscribe({
      next: (remote) => {
        const current = this.userSignal();
        const merged: AuthUser = {
          id: remote.id,
          name: remote.name,
          email: remote.email,
          role: remote.role,
          treatmentsCompleted:
            current?.id === remote.id ? current.treatmentsCompleted : 0,
        };
        this.userSignal.set(merged);
        this.writeStorage(this.tokenSignal(), merged);
      },
      error: (error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.clearSession();
        }
      },
    });
  }

  private persist(response: AuthResponse): void {
    const previous = this.userSignal();
    const user: AuthUser = {
      id: response.user.id,
      name: response.user.name,
      email: response.user.email,
      role: response.user.role,
      treatmentsCompleted: previous?.id === response.user.id ? previous.treatmentsCompleted : 0,
    };
    this.tokenSignal.set(response.accessToken);
    this.userSignal.set(user);
    this.writeStorage(response.accessToken, user, Date.now() + response.expiresIn * 1000);
  }

  private restore(): void {
    try {
      const raw = localStorage.getItem(AUTH_STORAGE_KEY);
      if (!raw) {
        return;
      }
      const stored = JSON.parse(raw) as StoredSession;
      if (!stored.accessToken || !stored.user?.id || stored.expiresAt <= Date.now()) {
        localStorage.removeItem(AUTH_STORAGE_KEY);
        return;
      }
      this.tokenSignal.set(stored.accessToken);
      this.userSignal.set(stored.user);
    } catch {
      this.clearSession();
    }
  }

  private writeStorage(token: string | null, user: AuthUser | null, expiresAt?: number): void {
    if (!token || !user) {
      return;
    }
    try {
      const existing = this.readExpiresAt();
      const session: StoredSession = {
        accessToken: token,
        expiresAt: expiresAt ?? existing ?? Date.now() + 8 * 60 * 60 * 1000,
        user,
      };
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
    } catch {
      /* ignore */
    }
  }

  private readExpiresAt(): number | null {
    try {
      const raw = localStorage.getItem(AUTH_STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const stored = JSON.parse(raw) as StoredSession;
      return typeof stored.expiresAt === 'number' ? stored.expiresAt : null;
    } catch {
      return null;
    }
  }
}
