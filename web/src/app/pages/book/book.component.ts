import { Component, computed, effect, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BookingService } from '../../core/booking.service';
import { LocalizedPricePipe, TranslatePipe } from '../../core/i18n.pipes';
import { LocaleService } from '../../core/locale.service';
import { LoyaltyService } from '../../core/loyalty.service';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-book',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe, LocalizedPricePipe],
  templateUrl: './book.component.html',
  styleUrl: './book.component.scss',
})
export class BookComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly booking = inject(BookingService);
  private readonly tenant = inject(TenantStore);
  private readonly i18n = inject(LocaleService);
  readonly auth = inject(AuthService);
  readonly loyalty = inject(LoyaltyService);

  readonly discountPct = computed(() => Math.round(this.loyalty.rewardDiscount() * 100));

  submitted = false;
  readonly minDate = this.todayLocal();
  readonly services = this.tenant.services;
  readonly times = this.tenant.timeSlots;

  readonly form = this.fb.nonNullable.group({
    petName: ['', [Validators.required, Validators.minLength(2)]],
    service: ['', Validators.required],
    date: [this.minDate, Validators.required],
    time: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const list = this.services();
      const current = this.form.controls.service.value;
      if (!current && list.length > 0) {
        this.form.controls.service.setValue(list[0].id);
      }
    });
  }

  selectTime(time: string): void {
    this.form.controls.time.setValue(time);
  }

  private todayLocal(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }

  submit(): void {
    this.submitted = true;
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const service = this.tenant.serviceById(value.service);
    const discounted = this.loyalty.isRewardVisit();
    const user = this.auth.user();
    this.booking.save({
      petName: value.petName,
      service: service?.label ?? value.service,
      serviceId: value.service,
      date: value.date,
      time: value.time,
      discounted,
      price: this.tenant.priceFor(value.service, discounted),
      ownerName: user?.name ?? this.i18n.t('book.guest'),
      ownerEmail: user?.email ?? '',
    });
    void this.router.navigateByUrl('/confirmation');
  }
}
