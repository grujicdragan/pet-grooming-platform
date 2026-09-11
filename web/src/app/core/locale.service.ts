import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { en } from '../i18n/en';
import {
  AppLocale,
  LOCALE_META,
  LOCALE_STORAGE_KEY,
  detectBrowserLocale,
  isAppLocale,
  readStoredLocale,
} from '../i18n/locale-storage';
import { ru } from '../i18n/ru';
import { Dictionary, TranslationKey, sr } from '../i18n/sr';
import { TenantStore } from './tenant.store';

const DICTIONARIES: Record<AppLocale, Dictionary> = { sr, en, ru };
const FALLBACK: AppLocale = 'sr';

/** Tenant brand fonts are Latin-only; these fallbacks cover Cyrillic (see BrandingApplier). */
const CYRILLIC_LOCALES: readonly AppLocale[] = ['ru'];
const CYRILLIC_FONTS_ID = 'cyrillic-fallback-fonts';
const CYRILLIC_FONTS_HREF =
  'https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,700;1,700&family=Manrope:wght@400;500;600&display=swap';

/**
 * Runtime i18n for UI strings + locale-aware formatting.
 *
 * - `preference`: what the user picked (or browser language) — persisted.
 * - `locale`: what is actually in effect — limited to the locales the tenant
 *   offers via its `localization` feature; tenants without the feature stay
 *   on their default language.
 * - Tenant *content* is translated server-side; TenantStore refetches with
 *   `?locale=` whenever `locale` changes.
 */
@Injectable({ providedIn: 'root' })
export class LocaleService {
  private readonly doc = inject(DOCUMENT);
  private readonly tenant = inject(TenantStore);

  readonly preference = signal<AppLocale>(readStoredLocale() ?? detectBrowserLocale() ?? FALLBACK);

  private readonly localization = computed(() => this.tenant.features()['localization'] ?? null);

  readonly tenantDefault = computed<AppLocale>(() => {
    const value = this.localization()?.config?.['default'];
    return isAppLocale(value) ? value : FALLBACK;
  });

  /** Locales the tenant offers; empty when localization is off. */
  readonly available = computed<AppLocale[]>(() => {
    const feature = this.localization();
    if (!feature?.enabled) {
      return [];
    }
    const list = feature.config?.['locales'];
    return Array.isArray(list) ? list.filter(isAppLocale) : [];
  });

  /** Show the switcher only when there is something to switch between. */
  readonly switchable = computed(() => this.available().length > 1);

  readonly locale = computed<AppLocale>(() => {
    if (this.tenant.loading()) {
      return this.preference();
    }
    const available = this.available();
    if (available.length === 0) {
      return this.tenantDefault();
    }
    const preferred = this.preference();
    if (available.includes(preferred)) {
      return preferred;
    }
    return available.includes(this.tenantDefault()) ? this.tenantDefault() : available[0];
  });

  readonly meta = computed(() => LOCALE_META[this.locale()]);
  private readonly dictionary = computed(() => DICTIONARIES[this.locale()]);

  constructor() {
    effect(() => {
      this.doc.documentElement.lang = this.meta().tag;
    });
    effect(() => {
      this.tenant.setRequestLocale(this.locale());
    });
    effect(() => {
      if (CYRILLIC_LOCALES.includes(this.locale())) {
        this.ensureCyrillicFonts();
      }
    });
  }

  private ensureCyrillicFonts(): void {
    if (this.doc.getElementById(CYRILLIC_FONTS_ID)) {
      return;
    }
    const link = this.doc.createElement('link');
    link.id = CYRILLIC_FONTS_ID;
    link.rel = 'stylesheet';
    link.href = CYRILLIC_FONTS_HREF;
    this.doc.head.appendChild(link);
  }

  set(locale: AppLocale): void {
    this.preference.set(locale);
    try {
      localStorage.setItem(LOCALE_STORAGE_KEY, locale);
    } catch {
      /* storage unavailable — in-memory only */
    }
  }

  /** Translate a UI key with optional `{param}` interpolation. Unknown keys return the key. */
  t(key: TranslationKey | string, params?: Record<string, string | number | null | undefined>): string {
    const dict = this.dictionary() as Record<string, string>;
    const template = dict[key] ?? (sr as Record<string, string>)[key] ?? key;
    if (!params) {
      return template;
    }
    return template.replace(/\{(\w+)\}/g, (match, name: string) => {
      const value = params[name];
      return value === null || value === undefined ? match : String(value);
    });
  }

  formatPrice(amount: number, currency = 'RSD'): string {
    return new Intl.NumberFormat(this.meta().tag, {
      style: 'currency',
      currency,
      maximumFractionDigits: 0,
    }).format(amount);
  }

  formatDate(value: string | Date, style: 'short' | 'full' = 'full'): string {
    const date = typeof value === 'string' ? parseDate(value) : value;
    if (Number.isNaN(date.getTime())) {
      return typeof value === 'string' ? value : '';
    }
    const options: Intl.DateTimeFormatOptions =
      style === 'short'
        ? { day: 'numeric', month: 'short' }
        : { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
    return new Intl.DateTimeFormat(this.meta().tag, options).format(date);
  }

  formatNumber(value: number, maximumFractionDigits = 0): string {
    return new Intl.NumberFormat(this.meta().tag, { maximumFractionDigits }).format(value);
  }
}

/** Date-only strings (`YYYY-MM-DD`) are parsed in local time to avoid UTC day shifts. */
function parseDate(value: string): Date {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (match) {
    return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
  }
  return new Date(value);
}
