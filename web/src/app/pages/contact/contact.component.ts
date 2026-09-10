import { Component, computed, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TenantStore } from '../../core/tenant.store';

@Component({
  selector: 'app-contact',
  standalone: true,
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss',
})
export class ContactComponent {
  private readonly tenant = inject(TenantStore);
  private readonly sanitizer = inject(DomSanitizer);

  readonly salon = computed(() => this.tenant.salon());
  readonly instagram = computed(() => this.tenant.instagram());
  readonly mapUrl = computed<SafeResourceUrl | null>(() => {
    const embed = this.salon()?.mapEmbedUrl;
    return embed ? this.sanitizer.bypassSecurityTrustResourceUrl(embed) : null;
  });
}
