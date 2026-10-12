import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar />
    <main class="contenedor">
      <router-outlet />
    </main>
  `,
  styles: [`
    main {
      padding-top: 24px;
      padding-bottom: 60px;
      min-height: calc(100vh - 64px);
    }
  `]
})
export class App {}