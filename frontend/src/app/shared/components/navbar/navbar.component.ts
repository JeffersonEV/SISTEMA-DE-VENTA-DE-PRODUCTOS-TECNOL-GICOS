import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { TemaService } from '../../../core/services/tema.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="nav">
      <div class="contenedor nav__inner">
        <a routerLink="/" class="nav__logo">Tech<span>Store</span></a>

        <button class="nav__toggle" (click)="menuAbierto.set(!menuAbierto())"
                [attr.aria-expanded]="menuAbierto()" aria-label="Abrir menú de navegación">
          @if (menuAbierto()) {
            <span>&#10005;</span>
          } @else {
            <span>&#9776;</span>
          }
        </button>

        <nav class="nav__links" [class.nav__links--abierto]="menuAbierto()" (click)="menuAbierto.set(false)">
          <a routerLink="/" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
          <a routerLink="/catalogo" routerLinkActive="activo">Catálogo</a>
          @if (esAdmin()) {
            <a routerLink="/admin" routerLinkActive="activo">Panel admin</a>
          }
          @if (logueado() && !esAdmin()) {
            <a routerLink="/mis-pedidos" routerLinkActive="activo">Mis pedidos</a>
          }
        </nav>

        <div class="nav__actions">
          <button class="btn-tema" (click)="tema.alternar()" [attr.title]="tema.esOscuro() ? 'Tema claro' : 'Tema oscuro'"
                  [attr.aria-label]="tema.esOscuro() ? 'Activar tema claro' : 'Activar tema oscuro'">
            @if (tema.esOscuro()) { {{ '\u2600' }} } @else { {{ '\u263D' }} }
          </button>
          <a routerLink="/carrito" class="nav__carrito">
            <span class="icono">&#128722;</span>
            @if (carritoCount() > 0) {
              <span class="nav__badge">{{ carritoCount() }}</span>
            }
          </a>
          @if (logueado()) {
            <span class="nav__usuario">{{ usuario()?.nombre }}
              <span [class]="'badge badge-' + (usuario()?.rol === 'ADMIN' ? 'admin' : 'cliente')">{{ usuario()?.rol }}</span>
            </span>
            <button class="nav__logout" (click)="salir()">Salir</button>
          } @else {
            <a routerLink="/login" class="nav__login">Ingresar</a>
            <a routerLink="/registro" class="nav__registro">Registrarse</a>
          }
        </div>
      </div>
    </header>
  `,
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  private auth = inject(AuthService);
  private carrito = inject(CarritoService);
  readonly tema = inject(TemaService);

  readonly usuario = this.auth.user;
  readonly carritoCount = this.carrito.cantidadTotal;
  readonly menuAbierto = signal(false);

  logueado(): boolean {
    return this.auth.isLogueado();
  }

  esAdmin(): boolean {
    return this.auth.esAdmin();
  }

  salir(): void {
    this.auth.logout();
  }
}
