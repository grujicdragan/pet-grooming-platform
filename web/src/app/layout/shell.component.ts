import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { TenantStore } from '../core/tenant.store';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  readonly tenant = inject(TenantStore);
  readonly menuOpen = signal(false);

  readonly logoUrl = computed(() => this.tenant.branding()?.logoUrl ?? 'images/logo.png');
  readonly footerText = computed(() => {
    const salon = this.tenant.salon();
    if (!salon) {
      return '';
    }
    const discount = Math.round(this.tenant.loyaltyRewardDiscount() * 100);
    const per = this.tenant.loyaltyTreatmentsPerReward();
    return `${salon.displayName} · ${salon.address} · svaki ${per}. tretman ${discount}% popusta`;
  });

  constructor() {
    inject(Router)
      .events.pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.closeMenu());
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    this.auth.logout();
    this.closeMenu();
  }
}
