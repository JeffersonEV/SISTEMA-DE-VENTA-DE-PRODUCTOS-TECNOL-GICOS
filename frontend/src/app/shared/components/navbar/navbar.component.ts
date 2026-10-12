import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CarritoService } from '../../../core/services/carrito.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="nav">
      <div class="contenedor nav__inner">
        <a routerLink="/" class="nav__logo">Tech<span>Store</span></a>

        <nav class="nav__links">
          <a routerLink="/catalogo" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">Catálogo</a>
          @if (esAdmin()) {
            <a routerLink="/admin" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">Dashboard</a>
            <a routerLink="/admin/productos" routerLinkActive="activo">Productos</a>
            <a routerLink="/admin/pedidos" routerLinkActive="activo">Pedidos</a>
            <a routerLink="/admin/usuarios" routerLinkActive="activo">Usuarios</a>
            <a routerLink="/admin/reportes" routerLinkActive="activo">Reportes</a>
          }
          @if (logueado()) {
          <a routerLink="/mis-pedidos" routerLinkActive="activo">Mis pedidos</a>
          }
        </nav>

        <div class="nav__actions">
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
  private router = inject(Router);

  readonly usuario = this.auth.user;
  readonly carritoCount = this.carrito.cantidadTotal;

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