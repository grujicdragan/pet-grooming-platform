import { Component, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  templateUrl: './testimonials.component.html',
  styleUrl: './testimonials.component.scss',
})
export class TestimonialsComponent implements OnDestroy {
  private readonly tenant = inject(TenantStore);

  readonly items = computed(() => this.tenant.testimonials());
  readonly intervalMs = 6500;
  readonly index = signal(0);
  readonly paused = signal(false);

  private timer?: ReturnType<typeof setInterval>;

  constructor() {
    effect(() => {
      const count = this.items().length;
      if (count === 0) {
        this.stopTimer();
        return;
      }
      if (this.index() >= count) {
        this.index.set(0);
      }
      if (!this.paused()) {
        this.startTimer();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  next(): void {
    const count = this.items().length;
    if (count === 0) {
      return;
    }
    this.index.update((value) => (value + 1) % count);
    this.restartTimer();
  }

  prev(): void {
    const count = this.items().length;
    if (count === 0) {
      return;
    }
    this.index.update((value) => (value - 1 + count) % count);
    this.restartTimer();
  }

  goTo(i: number): void {
    if (i === this.index()) {
      return;
    }
    this.index.set(i);
    this.restartTimer();
  }

  pause(): void {
    this.paused.set(true);
    this.stopTimer();
  }

  resume(): void {
    if (!this.paused()) {
      return;
    }
    this.paused.set(false);
    this.startTimer();
  }

  private startTimer(): void {
    this.stopTimer();
    const count = this.items().length;
    if (count === 0) {
      return;
    }
    this.timer = setInterval(() => {
      this.index.update((value) => (value + 1) % this.items().length);
    }, this.intervalMs);
  }

  private stopTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }

  private restartTimer(): void {
    if (!this.paused()) {
      this.startTimer();
    }
  }
}
