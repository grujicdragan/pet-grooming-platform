import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { TenantStore } from './tenant.store';

export type ThemePreference = 'light' | 'dark' | 'system';

/** Must match the no-flash script in index.html. */
const STORAGE_KEY = 'pgp-theme';
const THEME_COLORS = { light: '#f6efe4', dark: '#131715' } as const;

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly tenant = inject(TenantStore);
  private readonly doc = inject(DOCUMENT);
  private readonly mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  private readonly systemDark = signal(this.mediaQuery.matches);

  /** Explicit user choice; 'system' follows the OS setting. */
  readonly preference = signal<ThemePreference>(this.readStored());

  /**
   * Whether the current tenant offers dark mode.
   * `null` while tenant data is loading, so we keep whatever the no-flash
   * script applied instead of flickering back to light.
   */
  readonly enabled = computed<boolean | null>(() => {
    if (this.tenant.loading()) {
      return null;
    }
    return this.tenant.features()['dark_mode']?.enabled ?? false;
  });

  readonly isDark = computed(() => {
    if (this.enabled() === false) {
      return false;
    }
    const pref = this.preference();
    return pref === 'dark' || (pref === 'system' && this.systemDark());
  });

  constructor() {
    this.mediaQuery.addEventListener('change', (event) => this.systemDark.set(event.matches));

    effect(() => {
      const dark = this.isDark();
      const root = this.doc.documentElement;
      if (dark) {
        root.setAttribute('data-theme', 'dark');
      } else {
        root.removeAttribute('data-theme');
      }
      this.updateThemeColorMeta(dark);
    });
  }

  toggle(): void {
    this.setPreference(this.isDark() ? 'light' : 'dark');
  }

  setPreference(preference: ThemePreference): void {
    this.withTransition();
    this.preference.set(preference);
    try {
      if (preference === 'system') {
        localStorage.removeItem(STORAGE_KEY);
      } else {
        localStorage.setItem(STORAGE_KEY, preference);
      }
    } catch {
      /* storage unavailable (private mode / disabled) — keep in-memory only */
    }
  }

  private readStored(): ThemePreference {
    try {
      const value = localStorage.getItem(STORAGE_KEY);
      return value === 'light' || value === 'dark' ? value : 'system';
    } catch {
      return 'system';
    }
  }

  private updateThemeColorMeta(dark: boolean): void {
    let meta = this.doc.querySelector<HTMLMetaElement>('meta[name="theme-color"]');
    if (!meta) {
      meta = this.doc.createElement('meta');
      meta.name = 'theme-color';
      this.doc.head.appendChild(meta);
    }
    meta.content = dark ? THEME_COLORS.dark : THEME_COLORS.light;
  }

  /** Cross-fade colors on manual toggle; skipped for reduced-motion users. */
  private withTransition(): void {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }
    const root = this.doc.documentElement;
    root.classList.add('theme-transition');
    window.setTimeout(() => root.classList.remove('theme-transition'), 350);
  }
}
