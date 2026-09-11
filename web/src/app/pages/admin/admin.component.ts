import { Component, computed, inject, signal } from '@angular/core';
import { BookingService, BookingStatus } from '../../core/booking.service';
import { LocalizedDatePipe, LocalizedPricePipe, TranslatePipe } from '../../core/i18n.pipes';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [TranslatePipe, LocalizedPricePipe, LocalizedDatePipe],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminComponent {
  private readonly booking = inject(BookingService);
  private readonly tenant = inject(TenantStore);
  readonly filter = signal<'all' | BookingStatus>('all');
  readonly filters: ('all' | BookingStatus)[] = ['all', 'scheduled', 'completed', 'cancelled'];

  readonly discountPct = computed(() => Math.round(this.tenant.loyaltyRewardDiscount() * 100));

  readonly rows = computed(() => {
    const status = this.filter();
    const list = this.booking.bookings();
    return status === 'all' ? list : list.filter((item) => item.status === status);
  });

  /** Service label in the current language (falls back to the label stored at booking time). */
  serviceLabel(serviceId: string, fallback: string): string {
    return this.tenant.serviceById(serviceId)?.label ?? fallback;
  }

  setFilter(status: 'all' | BookingStatus): void {
    this.filter.set(status);
  }

  complete(id: string): void {
    this.booking.setStatus(id, 'completed');
  }

  cancel(id: string): void {
    this.booking.setStatus(id, 'cancelled');
  }
}
