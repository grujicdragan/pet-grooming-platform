import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LocalizedPricePipe, TranslatePipe } from '../../core/i18n.pipes';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [RouterLink, TranslatePipe, LocalizedPricePipe],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss',
})
export class PricingComponent {
  private readonly tenant = inject(TenantStore);
  readonly services = computed(() => this.tenant.services());
  readonly loading = this.tenant.loading;
  readonly perReward = this.tenant.loyaltyTreatmentsPerReward;
}
