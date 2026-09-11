import { Pipe, PipeTransform, inject } from '@angular/core';
import { LocaleService } from './locale.service';

/**
 * Impure on purpose: the output depends on the `LocaleService.locale` signal,
 * not only on the pipe input. Lookups are O(1) so the cost is negligible.
 */
@Pipe({ name: 't', standalone: true, pure: false })
export class TranslatePipe implements PipeTransform {
  private readonly i18n = inject(LocaleService);

  transform(key: string, params?: Record<string, string | number | null | undefined>): string {
    return this.i18n.t(key, params);
  }
}

@Pipe({ name: 'price', standalone: true, pure: false })
export class LocalizedPricePipe implements PipeTransform {
  private readonly i18n = inject(LocaleService);

  transform(amount: number | null | undefined, currency = 'RSD'): string {
    return amount === null || amount === undefined ? '' : this.i18n.formatPrice(amount, currency);
  }
}

@Pipe({ name: 'ldate', standalone: true, pure: false })
export class LocalizedDatePipe implements PipeTransform {
  private readonly i18n = inject(LocaleService);

  transform(value: string | Date | null | undefined, style: 'short' | 'full' = 'full'): string {
    return value ? this.i18n.formatDate(value, style) : '';
  }
}
