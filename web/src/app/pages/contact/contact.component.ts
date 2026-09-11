import { Component, computed, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TranslatePipe } from '../../core/i18n.pipes';
import { LocaleService } from '../../core/locale.service';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss',
})
export class ContactComponent {
  private readonly tenant = inject(TenantStore);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly i18n = inject(LocaleService);

  readonly salon = computed(() => this.tenant.salon());
  readonly lede = computed(() => {
    const salon = this.salon();
    if (!salon) {
      return '';
    }
    // `t()` reads the locale signal, so this re-computes on language change.
    const location = salon.state ? this.i18n.t('contact.ledeLocation', { state: salon.state }) : '';
    return this.i18n.t('contact.lede', { name: salon.displayName, location });
  });
  readonly instagram = computed(() => this.tenant.instagram());
  readonly mapUrl = computed<SafeResourceUrl | null>(() => {
    const embed = this.salon()?.mapEmbedUrl;
    return embed ? this.sanitizer.bypassSecurityTrustResourceUrl(embed) : null;
  });
}
