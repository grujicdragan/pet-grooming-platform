import { Injectable, computed, signal } from '@angular/core';

export interface DemoUser {
  name: string;
  email: string;
  treatmentsCompleted: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSignal = signal<DemoUser | null>(null);

  readonly user = this.userSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.userSignal() !== null);

  login(email: string, _password: string): boolean {
    const name = this.nameFromEmail(email);
    this.userSignal.set({
      name,
      email,
      treatmentsCompleted: 4,
    });
    return true;
  }

  register(name: string, email: string, _password: string): boolean {
    this.userSignal.set({
      name,
      email,
      treatmentsCompleted: 0,
    });
    return true;
  }

  logout(): void {
    this.userSignal.set(null);
  }

  completeTreatment(): void {
    const current = this.userSignal();
    if (!current) {
      return;
    }
    this.userSignal.set({
      ...current,
      treatmentsCompleted: current.treatmentsCompleted + 1,
    });
  }

  private nameFromEmail(email: string): string {
    const local = email.split('@')[0] ?? 'gost';
    return local
      .replace(/[._-]+/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }
}
