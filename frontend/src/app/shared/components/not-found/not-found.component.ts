import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <div class="nf">
      <h1>404</h1>
      <p>La página que buscas no existe.</p>
      <a routerLink="/" class="boton boton-primario">Volver al inicio</a>
    </div>
  `,
  styles: [`
    .nf {
      text-align: center;
      padding: 80px 0;
      h1 {
        font-size: 5rem;
        font-weight: 800;
        color: #2563eb;
      }
      p {
        color: #64748b;
        margin-bottom: 24px;
      }
    }
  `]
})
export class NotFoundComponent {}