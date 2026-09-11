import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { TranslatePipe } from '../core/i18n.pipes';
import { LocaleService } from '../core/locale.service';
import { AppLocale, LOCALE_META } from '../i18n/locale-storage';

/**
 * Accessible language dropdown (button + listbox pattern).
 * Keyboard: ↑/↓ move, Enter/Space select, Esc/Tab close.
 */
@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [TranslatePipe],
  host: {
    '(document:click)': 'onDocumentClick($event)',
    '(keydown)': 'onKeydown($event)',
  },
  template: `
    <div class="lang">
      <button
        #trigger
        type="button"
        class="lang-btn"
        aria-haspopup="listbox"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="('lang.choose' | t) + ': ' + i18n.meta().native"
        [title]="'lang.choose' | t"
        (click)="toggle()"
      >
        <svg class="globe" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
          <circle cx="12" cy="12" r="9" />
          <path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18" />
        </svg>
        <span class="code">{{ i18n.meta().code }}</span>
        <svg class="chevron" [class.up]="open()" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="m6 9 6 6 6-6" />
        </svg>
      </button>

      @if (open()) {
        <ul
          class="lang-menu"
          role="listbox"
          [attr.aria-label]="'lang.label' | t"
          [attr.aria-activedescendant]="'lang-opt-' + i18n.available()[active()]"
        >
          @for (code of i18n.available(); track code; let i = $index) {
            <li
              role="option"
              [id]="'lang-opt-' + code"
              [attr.aria-selected]="code === i18n.locale()"
              [class.focused]="i === active()"
              [lang]="meta[code].tag"
              (click)="select(code)"
              (mouseenter)="active.set(i)"
            >
              <span class="native">{{ meta[code].native }}</span>
              <span class="opt-code">{{ meta[code].code }}</span>
              @if (code === i18n.locale()) {
                <svg class="check" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="m5 12 4.5 4.5L19 7" />
                </svg>
              }
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    :host {
      display: inline-flex;
    }

    .lang {
      position: relative;
    }

    .lang-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      height: 2.25rem;
      padding: 0 0.7rem 0 0.6rem;
      border: 1px solid var(--line);
      border-radius: 999px;
      background: var(--input-bg);
      color: var(--ink);
      font: inherit;
      font-weight: 600;
      font-size: 0.82rem;
      letter-spacing: 0.04em;
      cursor: pointer;
      transition: border-color 0.2s ease, background-color 0.2s ease;
    }

    .lang-btn:hover,
    .lang-btn[aria-expanded='true'] {
      border-color: color-mix(in srgb, var(--moss) 45%, var(--line));
      background: var(--leaf-soft);
    }

    .lang-btn:focus-visible {
      outline: 2px solid var(--focus-ring);
      outline-offset: 2px;
    }

    .globe {
      width: 1.05rem;
      height: 1.05rem;
      color: var(--moss);
    }

    .chevron {
      width: 0.85rem;
      height: 0.85rem;
      color: var(--muted);
      transition: transform 0.2s ease;
    }

    .chevron.up {
      transform: rotate(180deg);
    }

    .lang-menu {
      position: absolute;
      top: calc(100% + 0.45rem);
      right: 0;
      z-index: 20;
      min-width: 11rem;
      margin: 0;
      padding: 0.35rem;
      list-style: none;
      background: var(--card);
      border: 1px solid var(--line);
      border-radius: 1rem;
      box-shadow: var(--shadow-strong);
      animation: pop 0.16s cubic-bezier(0.2, 0.7, 0.2, 1);
      transform-origin: top right;
    }

    @keyframes pop {
      from {
        opacity: 0;
        transform: translateY(-4px) scale(0.98);
      }
      to {
        opacity: 1;
        transform: none;
      }
    }

    li {
      display: grid;
      grid-template-columns: 1fr auto auto;
      align-items: center;
      gap: 0.6rem;
      padding: 0.55rem 0.7rem;
      border-radius: 0.7rem;
      cursor: pointer;
      color: var(--ink);
      font-size: 0.92rem;
    }

    li.focused {
      background: var(--leaf-soft);
    }

    li[aria-selected='true'] .native {
      font-weight: 600;
    }

    .opt-code {
      font-size: 0.72rem;
      font-weight: 600;
      letter-spacing: 0.08em;
      color: var(--muted);
    }

    .check {
      width: 1rem;
      height: 1rem;
      color: var(--moss);
    }

    li:not([aria-selected='true']) .opt-code {
      grid-column: 2 / 4;
    }

    /* In the mobile menu the switcher sits at the left edge of the row, so a
       right-anchored popover would run off-screen — anchor it to the left. */
    @media (max-width: 980px) {
      .lang-menu {
        left: 0;
        right: auto;
        transform-origin: top left;
        max-width: calc(100vw - 2.2rem);
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .lang-menu {
        animation: none;
      }

      .chevron {
        transition: none;
      }
    }
  `,
})
export class LanguageSwitcherComponent {
  readonly i18n = inject(LocaleService);
  readonly meta = LOCALE_META;
  readonly open = signal(false);
  readonly active = signal(0);

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly trigger = viewChild.required<ElementRef<HTMLButtonElement>>('trigger');

  toggle(): void {
    this.open() ? this.close() : this.show();
  }

  select(code: AppLocale): void {
    this.i18n.set(code);
    this.close();
    this.trigger().nativeElement.focus();
  }

  onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.close();
    }
  }

  onKeydown(event: KeyboardEvent): void {
    const options = this.i18n.available();
    switch (event.key) {
      case 'ArrowDown':
      case 'ArrowUp': {
        event.preventDefault();
        if (!this.open()) {
          this.show();
          return;
        }
        const delta = event.key === 'ArrowDown' ? 1 : -1;
        this.active.set((this.active() + delta + options.length) % options.length);
        return;
      }
      case 'Home':
      case 'End':
        if (this.open()) {
          event.preventDefault();
          this.active.set(event.key === 'Home' ? 0 : options.length - 1);
        }
        return;
      case 'Enter':
      case ' ':
        if (this.open()) {
          event.preventDefault();
          this.select(options[this.active()]);
        }
        return;
      case 'Escape':
        if (this.open()) {
          event.preventDefault();
          this.close();
          this.trigger().nativeElement.focus();
        }
        return;
      case 'Tab':
        this.close();
        return;
      default:
        return;
    }
  }

  private show(): void {
    const index = this.i18n.available().indexOf(this.i18n.locale());
    this.active.set(Math.max(0, index));
    this.open.set(true);
  }

  private close(): void {
    this.open.set(false);
  }
}
