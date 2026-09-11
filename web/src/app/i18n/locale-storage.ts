export type AppLocale = 'sr' | 'en' | 'ru';

export const SUPPORTED_LOCALES: readonly AppLocale[] = ['sr', 'en', 'ru'];

export interface LocaleMeta {
  /** Short label shown in the switcher trigger. */
  code: string;
  /** Language name in its own language (never flags — language ≠ country). */
  native: string;
  /** BCP 47 tag for Intl formatting and <html lang>. */
  tag: string;
}

export const LOCALE_META: Record<AppLocale, LocaleMeta> = {
  sr: { code: 'SR', native: 'Srpski', tag: 'sr-Latn-RS' },
  en: { code: 'EN', native: 'English', tag: 'en-GB' },
  ru: { code: 'RU', native: 'Русский', tag: 'ru-RU' },
};

export const LOCALE_STORAGE_KEY = 'pgp-locale';

export function isAppLocale(value: unknown): value is AppLocale {
  return typeof value === 'string' && (SUPPORTED_LOCALES as readonly string[]).includes(value);
}

/** Explicit user choice, if any. Shared by LocaleService and TenantStore (first fetch). */
export function readStoredLocale(): AppLocale | null {
  try {
    const value = localStorage.getItem(LOCALE_STORAGE_KEY);
    return isAppLocale(value) ? value : null;
  } catch {
    return null;
  }
}

/** Best match from the browser's language list, or null. */
export function detectBrowserLocale(): AppLocale | null {
  const candidates = typeof navigator !== 'undefined' ? navigator.languages ?? [navigator.language] : [];
  for (const candidate of candidates) {
    const language = candidate.toLowerCase().split('-')[0];
    if (isAppLocale(language)) {
      return language;
    }
  }
  return null;
}
