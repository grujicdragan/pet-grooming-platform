import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  FeatureInfo,
  GroomingService,
  GalleryItem,
  SalonInfo,
  SiteSection,
  SocialLink,
  TenantSite,
  TestimonialItem,
} from './tenant.models';

@Injectable({ providedIn: 'root' })
export class TenantStore {
  private readonly http = inject(HttpClient);

  private readonly siteSignal = signal<TenantSite | null>(null);
  private readonly loadingSignal = signal(true);
  private readonly errorSignal = signal<string | null>(null);

  readonly site = this.siteSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();

  readonly salon = computed<SalonInfo | null>(() => this.siteSignal()?.salon ?? null);
  readonly branding = computed(() => this.siteSignal()?.branding ?? null);
  readonly services = computed<GroomingService[]>(() => this.siteSignal()?.services ?? []);
  readonly gallery = computed<GalleryItem[]>(() => this.siteSignal()?.gallery ?? []);
  readonly testimonials = computed<TestimonialItem[]>(() => this.siteSignal()?.testimonials ?? []);
  readonly socialLinks = computed<SocialLink[]>(() => this.siteSignal()?.socialLinks ?? []);
  readonly sections = computed<Record<string, SiteSection>>(
    () => this.siteSignal()?.sections ?? {},
  );
  readonly features = computed<Record<string, FeatureInfo>>(
    () => this.siteSignal()?.features ?? {},
  );

  readonly displayName = computed(
    () => this.salon()?.displayName ?? this.siteSignal()?.name ?? '',
  );
  readonly tagline = computed(() => this.salon()?.tagline ?? '');

  readonly instagram = computed(() =>
    this.socialLinks().find((link) => link.platform.toLowerCase() === 'instagram') ?? null,
  );

  readonly timeSlots = computed<string[]>(() => {
    const config = this.features()['online_booking']?.config;
    const slots = config?.['timeSlots'];
    return Array.isArray(slots) ? (slots as string[]) : [];
  });

  readonly loyaltyTreatmentsPerReward = computed(() => {
    const value = this.features()['loyalty']?.config?.['treatmentsPerReward'];
    return typeof value === 'number' ? value : 5;
  });

  readonly loyaltyRewardDiscount = computed(() => {
    const value = this.features()['loyalty']?.config?.['rewardDiscount'];
    return typeof value === 'number' ? value : 0.4;
  });

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);
    const url = `${environment.apiUrl}/api/public/tenants/${environment.tenantSlug}`;
    this.http
      .get<TenantSite>(url)
      .pipe(
        tap((site) => {
          this.siteSignal.set(site);
          this.loadingSignal.set(false);
        }),
        catchError((err) => {
          console.error('Failed to load tenant site', err);
          this.errorSignal.set('Ne mogu da učitam podatke salona. Proveri da li API radi.');
          this.loadingSignal.set(false);
          return of(null);
        }),
      )
      .subscribe();
  }

  priceFor(serviceId: string, discounted: boolean): number {
    const service = this.services().find(
      (item) => item.id === serviceId || item.code === serviceId,
    );
    const base = service?.price ?? 0;
    const discount = this.loyaltyRewardDiscount();
    return discounted ? Math.round(base * (1 - discount)) : base;
  }

  serviceById(serviceId: string): GroomingService | undefined {
    return this.services().find((item) => item.id === serviceId || item.code === serviceId);
  }
}
