import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { BookingService, BookingStatus } from '../../core/booking.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminComponent {
  private readonly booking = inject(BookingService);
  readonly filter = signal<'all' | BookingStatus>('all');
  readonly labels: Record<BookingStatus, string> = {
    scheduled: 'Zakazan',
    completed: 'Završen',
    cancelled: 'Otkazan',
  };

  readonly rows = computed(() => {
    const status = this.filter();
    const list = this.booking.bookings();
    return status === 'all' ? list : list.filter((item) => item.status === status);
  });

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
