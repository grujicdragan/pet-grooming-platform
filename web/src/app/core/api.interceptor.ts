import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toasts = inject(ToastService);
  const headers: Record<string, string> = {
    'X-Tenant-Slug': environment.tenantSlug,
  };
  const token = auth.token();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return next(req.clone({ setHeaders: headers })).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.includes('/api/auth/login') &&
        !req.url.includes('/api/auth/register')
      ) {
        auth.clearSession();
      }
      if (shouldToast(req.url, error)) {
        toasts.error(AuthService.errorKey(error));
      }
      return throwError(() => error);
    }),
  );
};

function shouldToast(url: string, error: unknown): boolean {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }
  if (url.includes('/api/public/')) {
    return false;
  }
  if (url.includes('/api/auth/login') || url.includes('/api/auth/register')) {
    return false;
  }
  if (url.includes('/api/auth/me') && error.status === 401) {
    return false;
  }
  return true;
}
