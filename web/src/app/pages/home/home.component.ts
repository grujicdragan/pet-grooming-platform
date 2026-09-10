import { Component, computed, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RevealDirective } from '../../core/reveal.directive';
import { TenantStore } from '../../core/tenant.store';
import { TestimonialsComponent } from './testimonials.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, RevealDirective, TestimonialsComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  readonly tenant = inject(TenantStore);

  readonly salon = computed(() => this.tenant.salon());
  readonly services = computed(() => this.tenant.services());
  readonly gallery = computed(() => this.tenant.gallery());
  readonly instagram = computed(() => this.tenant.instagram());

  readonly hero = computed(() => this.tenant.sections()['hero'] ?? null);
  readonly stats = computed(() => {
    const items = this.tenant.sections()['stats']?.content?.['items'];
    return Array.isArray(items) ? (items as { value: string; label: string }[]) : [];
  });
  readonly marquee = computed(() => {
    const items = this.tenant.sections()['marquee']?.content?.['items'];
    return Array.isArray(items) ? (items as string[]) : [];
  });
  readonly steps = computed(() => {
    const steps = this.tenant.sections()['booking_steps']?.content?.['steps'];
    return Array.isArray(steps) ? (steps as { title: string; text: string }[]) : [];
  });

  readonly heroImage = computed(
    () => this.hero()?.imageUrl ?? this.tenant.branding()?.coverImageUrl ?? 'images/hero-dogs.jpg',
  );
  readonly heroBadge = computed(() => {
    const badge = this.hero()?.content?.['badge'];
    return typeof badge === 'string' ? badge : this.salon()?.address ?? '';
  });
  readonly heroTitleHtml = computed(() => this.hero()?.subtitle ?? this.hero()?.title ?? '');
  readonly heroBody = computed(() => this.hero()?.body ?? this.salon()?.description ?? '');
  readonly floatTitle = computed(() => {
    const value = this.hero()?.content?.['floatTitle'];
    return typeof value === 'string' ? value : '';
  });
  readonly floatSubtitle = computed(() => {
    const value = this.hero()?.content?.['floatSubtitle'];
    return typeof value === 'string' ? value : '';
  });

  readonly loyaltyDiscountPct = computed(() =>
    Math.round(this.tenant.loyaltyRewardDiscount() * 100),
  );
  readonly loyaltyPerReward = computed(() => this.tenant.loyaltyTreatmentsPerReward());
  readonly stamps = computed(() =>
    Array.from({ length: this.loyaltyPerReward() }, (_, i) => i + 1),
  );
}
