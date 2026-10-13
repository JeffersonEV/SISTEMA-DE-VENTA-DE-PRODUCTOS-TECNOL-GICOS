import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TemaService } from '../../../core/services/tema.service';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="admin-shell" [class.admin-shell--cerrado]="!abierto()">
      <aside class="admin-side" [class.admin-side--abierta]="abierto()">
        <div class="admin-side__marca">
          <button class="admin-side__hamburguesa" (click)="toggle()"
                  [attr.title]="abierto() ? 'Ocultar menú' : 'Mostrar menú'"
                  [attr.aria-label]="abierto() ? 'Ocultar menú' : 'Mostrar menú'">
            @if (abierto()) {
              <span class="admin-side__hamburguesa-ico">&#10005;</span>
            } @else {
              <span class="admin-side__hamburguesa-ico">&#9776;</span>
            }
          </button>
          <div class="admin-side__marca-txt" [class.admin-side__marca-txt--oculto]="!abierto()">
            <span class="admin-side__logo">Tech<span>Store</span></span>
            <span class="admin-side__rol">Panel ADMIN</span>
          </div>
        </div>
        <nav class="admin-side__menu">
          <a routerLink="/admin" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }" title="Dashboard">
            <span class="admin-side__ico">&#128202;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Dashboard</span>
          </a>
          <a routerLink="/admin/productos" routerLinkActive="activo" title="Productos">
            <span class="admin-side__ico">&#128230;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Productos</span>
          </a>
          <a routerLink="/admin/categorias" routerLinkActive="activo" title="Categorías">
            <span class="admin-side__ico">&#128193;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Categorías</span>
          </a>
          <a routerLink="/admin/pedidos" routerLinkActive="activo" title="Pedidos">
            <span class="admin-side__ico">&#128203;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Pedidos</span>
          </a>
          <a routerLink="/admin/usuarios" routerLinkActive="activo" title="Usuarios">
            <span class="admin-side__ico">&#128101;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Usuarios</span>
          </a>
          <a routerLink="/admin/reportes" routerLinkActive="activo" title="Reportes">
            <span class="admin-side__ico">&#128200;</span>
            <span class="admin-side__etiqueta" [class.admin-side__etiqueta--oculta]="!abierto()">Reportes</span>
          </a>
        </nav>
        <div class="admin-side__pie">
          <span class="admin-side__usuario" [class.admin-side__usuario--oculto]="!abierto()">{{ usuario()?.nombre }}</span>
          <button class="btn-tema" (click)="tema.alternar()"
                  [attr.title]="tema.esOscuro() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'">
            {{ tema.esOscuro() ? '\u2600' : '\u263D' }}
          </button>
          <button class="admin-side__salir" (click)="salir()" title="Cerrar sesión">
            <span class="admin-side__salir-ico">&#9211;</span>
            <span class="admin-side__salir-txt" [class.admin-side__salir-txt--oculta]="!abierto()">Cerrar sesión</span>
          </button>
        </div>
      </aside>

      <section class="admin-content">
        <div class="admin-content__barra">
          <button class="admin-content__burger" (click)="toggle()" title="Menú">
            <span class="admin-content__burger-ico">&#9776;</span>
          </button>
        </div>
        <router-outlet />
      </section>
    </div>
  `,
  styleUrl: './admin-layout.component.scss'
})
export class AdminLayoutComponent {
  private auth = inject(AuthService);
  readonly tema = inject(TemaService);
  readonly usuario = this.auth.user;

  readonly abierto = signal(window.innerWidth > 900);

  toggle(): void {
    this.abierto.update((a) => !a);
  }

  salir(): void {
    this.auth.logout();
  }
}
