import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TranslatePipe } from '../../core/i18n.pipes';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="panel auth">
      <p class="eyebrow">{{ 'verify.eyebrow' | t }}</p>
      <h1>{{ 'verify.title' | t }}</h1>
      @if (missing()) {
        <p class="lede">{{ 'verify.missing' | t }}</p>
      }
      <p class="switch"><a routerLink="/login">{{ 'register.login' | t }}</a></p>
    </section>
  `,
  styles: `
    .auth {
      max-width: 32rem;
      margin: 0 auto;
      padding: 2rem;
    }
    .switch {
      margin-top: 1.2rem;
    }
    .switch a {
      color: var(--moss);
      font-weight: 600;
    }
  `,
})
export class VerifyEmailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly toasts = inject(ToastService);

  readonly missing = signal(false);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.missing.set(true);
      return;
    }
    this.auth.verifyEmail(token).subscribe({
      next: () => {
        this.toasts.success('auth.verifyOk');
        void this.router.navigateByUrl('/login');
      },
      error: (error: unknown) => {
        this.toasts.error(AuthService.errorKey(error));
      },
    });
  }
}
