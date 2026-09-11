import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BookingService } from '../../core/booking.service';
import { LocalizedDatePipe, LocalizedPricePipe, TranslatePipe } from '../../core/i18n.pipes';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [RouterLink, TranslatePipe, LocalizedPricePipe, LocalizedDatePipe],
  templateUrl: './confirmation.component.html',
  styleUrl: './confirmation.component.scss',
})
export class ConfirmationComponent {
  readonly booking = inject(BookingService);
  readonly tenant = inject(TenantStore);
  private readonly auth = inject(AuthService);

  readonly discountPct = computed(() => Math.round(this.tenant.loyaltyRewardDiscount() * 100));

  /** Service label in the current language (falls back to the label stored at booking time). */
  serviceLabel(serviceId: string, fallback: string): string {
    return this.tenant.serviceById(serviceId)?.label ?? fallback;
  }

  finishVisit(): void {
    const last = this.booking.lastBooking();
    if (last) {
      this.auth.completeTreatment();
      this.booking.setStatus(last.id, 'completed');
    }
  }
}
