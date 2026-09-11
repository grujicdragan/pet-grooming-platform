import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TranslatePipe } from '../../core/i18n.pipes';
import { LoyaltyService } from '../../core/loyalty.service';

@Component({
  selector: 'app-loyalty',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './loyalty.component.html',
  styleUrl: './loyalty.component.scss',
})
export class LoyaltyComponent {
  readonly loyalty = inject(LoyaltyService);
  readonly auth = inject(AuthService);
  readonly cycle = computed(() => this.loyalty.treatmentsPerReward());
  readonly discountPercent = computed(() => Math.round(this.loyalty.rewardDiscount() * 100));
}
