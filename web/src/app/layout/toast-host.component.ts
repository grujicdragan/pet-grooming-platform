import { Component, inject } from '@angular/core';
import { TranslatePipe } from '../core/i18n.pipes';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <div class="toast-stack" aria-live="polite" aria-relevant="additions">
      @for (toast of toasts.messages(); track toast.id) {
        <div class="toast" [class.toast-error]="toast.kind === 'error'" [class.toast-success]="toast.kind === 'success'" [class.toast-info]="toast.kind === 'info'" role="alert">
          <p>{{ toast.key | t: toast.params }}</p>
          <button type="button" class="dismiss" [attr.aria-label]="'toast.dismiss' | t" (click)="toasts.dismiss(toast.id)">
            ×
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    :host {
      position: fixed;
      top: calc(var(--header-height, 5.05rem) + 0.5rem);
      right: 1.1rem;
      bottom: auto;
      left: auto;
      z-index: 10000;
      display: block;
      width: min(22.5rem, calc(100vw - 2.2rem));
      pointer-events: none;
    }

    .toast-stack {
      display: grid;
      gap: 0.55rem;
      width: 100%;
    }

    .toast {
      pointer-events: auto;
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 0.7rem;
      align-items: start;
      padding: 0.85rem 0.95rem;
      border-radius: 1rem;
      border: 1px solid var(--line);
      background: var(--card);
      box-shadow: var(--shadow-strong, 0 12px 32px rgba(0, 0, 0, 0.22));
      color: var(--ink);
      animation: toast-in 0.22s cubic-bezier(0.2, 0.7, 0.2, 1);
    }

    .toast p {
      margin: 0;
      font-size: 0.92rem;
      line-height: 1.4;
    }

    .toast-error {
      border-color: color-mix(in srgb, var(--error-fg) 45%, var(--line));
      background: color-mix(in srgb, var(--error-bg, #fdecea) 88%, var(--card));
    }

    .toast-success {
      border-color: color-mix(in srgb, var(--moss) 45%, var(--line));
      background: color-mix(in srgb, var(--leaf-soft) 70%, var(--card));
    }

    .dismiss {
      border: 0;
      background: transparent;
      color: var(--muted);
      font-size: 1.2rem;
      line-height: 1;
      cursor: pointer;
      padding: 0 0.15rem;
    }

    @media (min-width: 981px) {
      :host {
        right: 6vw;
      }
    }

    @keyframes toast-in {
      from {
        opacity: 0;
        transform: translateY(-8px);
      }
      to {
        opacity: 1;
        transform: none;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .toast {
        animation: none;
      }
    }
  `,
})
export class ToastHostComponent {
  readonly toasts = inject(ToastService);
}
