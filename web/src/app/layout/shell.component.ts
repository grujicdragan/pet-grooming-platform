import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { TranslatePipe } from '../core/i18n.pipes';
import { LocaleService } from '../core/locale.service';
import { TenantStore } from '../core/tenant.store';
import { ThemeService } from '../core/theme.service';
import { LanguageSwitcherComponent } from './language-switcher.component';
import { ThemeToggleComponent } from './theme-toggle.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ThemeToggleComponent, LanguageSwitcherComponent, TranslatePipe],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  readonly tenant = inject(TenantStore);
  readonly theme = inject(ThemeService);
  readonly i18n = inject(LocaleService);
  readonly menuOpen = signal(false);

  readonly logoUrl = computed(() => this.tenant.branding()?.logoUrl ?? 'images/logo.png');
  readonly footerText = computed(() => {
    const salon = this.tenant.salon();
    if (!salon) {
      return '';
    }
    const loyalty = this.i18n.t('footer.loyalty', {
      per: this.tenant.loyaltyTreatmentsPerReward(),
      discount: Math.round(this.tenant.loyaltyRewardDiscount() * 100),
    });
    return `${salon.displayName} · ${salon.address} · ${loyalty}`;
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
