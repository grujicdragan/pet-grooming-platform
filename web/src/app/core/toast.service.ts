import { Injectable, NgZone, inject, signal } from '@angular/core';

export type ToastKind = 'error' | 'success' | 'info';

export interface ToastMessage {
  id: number;
  kind: ToastKind;
  key: string;
  params?: Record<string, string | number>;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private readonly zone = inject(NgZone);
  private readonly messagesSignal = signal<ToastMessage[]>([]);

  readonly messages = this.messagesSignal.asReadonly();

  success(key: string, params?: Record<string, string | number>, ttlMs = 5200): void {
    this.push('success', key, params, ttlMs);
  }

  error(key: string, params?: Record<string, string | number>, ttlMs = 7000): void {
    this.push('error', key, params, ttlMs);
  }

  info(key: string, params?: Record<string, string | number>, ttlMs = 5200): void {
    this.push('info', key, params, ttlMs);
  }

  dismiss(id: number): void {
    this.zone.run(() => {
      this.messagesSignal.update((list) => list.filter((item) => item.id !== id));
    });
  }

  private push(
    kind: ToastKind,
    key: string,
    params: Record<string, string | number> | undefined,
    ttlMs: number,
  ): void {
    this.zone.run(() => {
      const id = this.nextId++;
      this.messagesSignal.update((list) => [...list.slice(-4), { id, kind, key, params }]);
      window.setTimeout(() => this.dismiss(id), ttlMs);
    });
  }
}
