import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { BookingService } from '../../core/booking.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './confirmation.component.html',
  styleUrl: './confirmation.component.scss',
})
export class ConfirmationComponent {
  readonly booking = inject(BookingService);
  private readonly auth = inject(AuthService);

  finishVisit(): void {
    const last = this.booking.lastBooking();
    if (last) {
      this.auth.completeTreatment();
      this.booking.setStatus(last.id, 'completed');
    }
  }
}
