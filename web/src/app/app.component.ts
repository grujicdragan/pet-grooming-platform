import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BrandingApplier } from './core/branding-applier';
import { ShellComponent } from './layout/shell.component';
import { ToastHostComponent } from './layout/toast-host.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ShellComponent, ToastHostComponent],
  template: `
    <app-shell>
      <router-outlet />
    </app-shell>
    <app-toast-host />
  `,
})
export class AppComponent {
  /** Applies tenant colors / fonts from API when site data loads. */
  private readonly _branding = inject(BrandingApplier);
}
