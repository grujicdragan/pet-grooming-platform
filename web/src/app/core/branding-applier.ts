import { effect, Injectable, inject } from '@angular/core';
import { TenantStore } from './tenant.store';

const FONT_HREF: Record<string, string> = {
  'Fraunces, Outfit':
    'https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,500;9..144,700&family=Outfit:wght@400;500;600&display=swap',
  'Syne, Manrope':
    'https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600&family=Syne:wght@500;700&display=swap',
};

@Injectable({ providedIn: 'root' })
export class BrandingApplier {
  private readonly tenant = inject(TenantStore);
  private readonly fontLinkId = 'tenant-font-stylesheet';

  constructor() {
    effect(() => {
      const branding = this.tenant.branding();
      const salon = this.tenant.salon();
      if (!branding && !salon) {
        return;
      }
      this.applyColors(branding);
      this.applyFonts(branding?.fontFamily ?? null);
      this.applyDocumentMeta(salon?.displayName ?? null, salon?.tagline ?? null, branding?.logoUrl ?? null);
    });
  }

  /**
   * Only the brand *sources* are set inline. Semantic tokens (--moss, --ink,
   * --leaf-soft, ...) derive from them in styles.scss, so the dark theme can
   * re-derive lighter/darker variants without inline styles overriding it.
   */
  private applyColors(branding: {
    primaryColor: string | null;
    secondaryColor: string | null;
    accentColor: string | null;
  } | null): void {
    const root = document.documentElement;
    if (!branding) {
      return;
    }
    if (branding.primaryColor) {
      root.style.setProperty('--brand-primary', branding.primaryColor);
    }
    if (branding.secondaryColor) {
      root.style.setProperty('--brand-secondary', branding.secondaryColor);
    }
    if (branding.accentColor) {
      root.style.setProperty('--brand-ink', branding.accentColor);
    }
  }

  private applyFonts(fontFamily: string | null): void {
    if (!fontFamily) {
      return;
    }
    const parts = fontFamily.split(',').map((part) => part.trim()).filter(Boolean);
    const serif = parts[0] ?? 'Georgia';
    const sans = parts[1] ?? parts[0] ?? 'system-ui';
    const root = document.documentElement;
    // Tenant fonts are Latin-only; the Cyrillic-capable fallbacks are loaded by
    // LocaleService when the UI switches to a Cyrillic locale (per-glyph fallback).
    root.style.setProperty('--serif', `"${serif}", "Playfair Display", Georgia, serif`);
    root.style.setProperty('--sans', `"${sans}", "Manrope", system-ui, sans-serif`);

    const href = FONT_HREF[fontFamily];
    if (!href) {
      return;
    }
    let link = document.getElementById(this.fontLinkId) as HTMLLinkElement | null;
    if (!link) {
      link = document.createElement('link');
      link.id = this.fontLinkId;
      link.rel = 'stylesheet';
      document.head.appendChild(link);
    }
    if (link.href !== href) {
      link.href = href;
    }
  }

  private applyDocumentMeta(
    displayName: string | null,
    tagline: string | null,
    logoUrl: string | null,
  ): void {
    if (displayName) {
      document.title = tagline ? `${displayName} — ${tagline}` : displayName;
    }
    if (logoUrl) {
      let icon = document.querySelector<HTMLLinkElement>("link[rel='icon']");
      if (!icon) {
        icon = document.createElement('link');
        icon.rel = 'icon';
        document.head.appendChild(icon);
      }
      icon.href = logoUrl;
      icon.type = logoUrl.endsWith('.svg') ? 'image/svg+xml' : 'image/png';
    }
  }
}
