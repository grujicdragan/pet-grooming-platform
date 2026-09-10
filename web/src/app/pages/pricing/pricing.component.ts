import { Component, computed, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss',
})
export class PricingComponent {
  private readonly tenant = inject(TenantStore);
  readonly services = computed(() => this.tenant.services());
  readonly loading = this.tenant.loading;
}
