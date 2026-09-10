import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LoyaltyService } from '../../core/loyalty.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-loyalty',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './loyalty.component.html',
  styleUrl: './loyalty.component.scss',
})
export class LoyaltyComponent {
  readonly loyalty = inject(LoyaltyService);
  readonly auth = inject(AuthService);
  readonly cycle = computed(() => this.loyalty.treatmentsPerReward());
  readonly discountPercent = computed(() => Math.round(this.loyalty.rewardDiscount() * 100));
}
