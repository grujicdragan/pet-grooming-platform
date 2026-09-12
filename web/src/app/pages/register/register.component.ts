import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { TranslatePipe } from '../../core/i18n.pipes';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly toasts = inject(ToastService);

  submitted = false;
  readonly busy = signal(false);
  pendingEmail: string | null = null;
  verifyUrl: string | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    this.submitted = true;
    if (this.form.invalid || this.busy()) {
      return;
    }

    this.busy.set(true);
    const { name, email, password } = this.form.getRawValue();
    this.auth
      .register(name, email, password)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (response) => {
          this.pendingEmail = response.email;
          this.verifyUrl = response.verifyUrl ?? null;
          if (response.emailSent) {
            this.toasts.success(this.verifyUrl ? 'register.checkEmailLocalToast' : 'auth.verifySent', {
              email: response.email,
            });
          } else {
            this.toasts.error('auth.verifySendFailed');
          }
        },
        error: (error: unknown) => {
          this.toasts.error(AuthService.errorKey(error));
        },
      });
  }
}
