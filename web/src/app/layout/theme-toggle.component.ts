import { Component, inject } from '@angular/core';
import { TranslatePipe } from '../core/i18n.pipes';
import { ThemeService } from '../core/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <button
      type="button"
      class="theme-toggle"
      role="switch"
      [attr.aria-checked]="theme.isDark()"
      [attr.aria-label]="(theme.isDark() ? 'theme.disableDark' : 'theme.enableDark') | t"
      [title]="(theme.isDark() ? 'theme.light' : 'theme.dark') | t"
      (click)="theme.toggle()"
    >
      <span class="track" aria-hidden="true">
        <svg class="icon sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        </svg>
        <svg class="icon moon" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
        </svg>
        <span class="thumb"></span>
      </span>
    </button>
  `,
  styles: `
    :host {
      display: inline-flex;
    }

    .theme-toggle {
      --w: 3.1rem;
      --h: 1.7rem;
      --pad: 0.18rem;
      --thumb: calc(var(--h) - var(--pad) * 2);

      display: inline-flex;
      align-items: center;
      padding: 0;
      border: 0;
      background: transparent;
      cursor: pointer;
      border-radius: 999px;
    }

    .theme-toggle:focus-visible {
      outline: 2px solid var(--focus-ring);
      outline-offset: 3px;
    }

    .track {
      position: relative;
      width: var(--w);
      height: var(--h);
      border-radius: 999px;
      background: var(--surface-2);
      border: 1px solid var(--line);
      display: grid;
      grid-template-columns: 1fr 1fr;
      align-items: center;
      transition: background-color 0.25s ease, border-color 0.25s ease;
    }

    .theme-toggle[aria-checked='true'] .track {
      background: var(--leaf-soft);
      border-color: color-mix(in srgb, var(--moss) 45%, var(--line));
    }

    .icon {
      width: 0.9rem;
      height: 0.9rem;
      justify-self: center;
      color: var(--muted);
      transition: opacity 0.25s ease, color 0.25s ease;
    }

    .sun {
      opacity: 1;
      color: var(--copper);
    }

    .moon {
      opacity: 0.55;
    }

    .theme-toggle[aria-checked='true'] .sun {
      opacity: 0.45;
      color: var(--muted);
    }

    .theme-toggle[aria-checked='true'] .moon {
      opacity: 1;
      color: var(--moss);
    }

    .thumb {
      position: absolute;
      top: var(--pad);
      left: var(--pad);
      width: var(--thumb);
      height: var(--thumb);
      border-radius: 50%;
      background: var(--card);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
      transition: transform 0.28s cubic-bezier(0.2, 0.7, 0.2, 1), background-color 0.25s ease;
    }

    .theme-toggle[aria-checked='true'] .thumb {
      transform: translateX(calc(var(--w) - var(--thumb) - var(--pad) * 2 - 2px));
      background: var(--moss);
    }

    .theme-toggle:hover .thumb {
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.24);
    }

    @media (prefers-reduced-motion: reduce) {
      .track,
      .icon,
      .thumb {
        transition: none;
      }
    }
  `,
})
export class ThemeToggleComponent {
  readonly theme = inject(ThemeService);
}
