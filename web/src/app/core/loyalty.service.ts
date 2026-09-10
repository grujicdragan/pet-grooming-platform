import { Injectable, computed, inject } from '@angular/core';
import { AuthService } from './auth.service';
import { TenantStore } from './tenant.store';

@Injectable({ providedIn: 'root' })
export class LoyaltyService {
  private readonly auth = inject(AuthService);
  private readonly tenant = inject(TenantStore);

  readonly treatmentsPerReward = computed(() => this.tenant.loyaltyTreatmentsPerReward());
  readonly rewardDiscount = computed(() => this.tenant.loyaltyRewardDiscount());

  readonly treatmentsCompleted = computed(
    () => this.auth.user()?.treatmentsCompleted ?? 0,
  );

  readonly progressInCycle = computed(() => {
    const perReward = this.treatmentsPerReward();
    return perReward === 0 ? 0 : this.treatmentsCompleted() % perReward;
  });

  readonly stamps = computed(() => {
    const perReward = this.treatmentsPerReward();
    const filled = this.progressInCycle();
    return Array.from({ length: perReward }, (_, index) => ({
      number: index + 1,
      filled: index < filled,
      reward: index === perReward - 1,
    }));
  });

  readonly nextIsReward = computed(() => {
    const perReward = this.treatmentsPerReward();
    return perReward > 0 && this.progressInCycle() === perReward - 1;
  });

  readonly treatmentsUntilReward = computed(() => {
    const perReward = this.treatmentsPerReward();
    const remaining = perReward - this.progressInCycle();
    return remaining === perReward ? perReward : remaining;
  });

  isRewardVisit(): boolean {
    return this.nextIsReward();
  }
}
