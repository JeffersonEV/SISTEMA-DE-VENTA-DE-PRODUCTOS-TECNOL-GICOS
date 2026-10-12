import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { ResumenReporte } from '../../../core/models/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Panel de administración</h1>
    <p class="subtitulo-pagina">Resumen general de la tienda.</p>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando…</div>
    } @else if (resumen()) {
      <section class="kpis">
        <div class="tarjeta kpi kpi-ventas">
          <span class="kpi__label">Total ventas</span>
          <span class="kpi__valor">S/ {{ resumen()?.totalVentas | number: '1.2-2' }}</span>
        </div>
        <div class="tarjeta kpi">
          <span class="kpi__label">Pedidos</span>
          <span class="kpi__valor">{{ resumen()?.totalPedidos }}</span>
          <span class="kpi__sub">
            {{ resumen()?.pedidosPendientes }} pendientes · {{ resumen()?.pedidosPagados }} pagados
          </span>
        </div>
        <div class="tarjeta kpi">
          <span class="kpi__label">Usuarios</span>
          <span class="kpi__valor">{{ resumen()?.totalUsuarios }}</span>
        </div>
        <div class="tarjeta kpi">
          <span class="kpi__label">Productos</span>
          <span class="kpi__valor">{{ resumen()?.totalProductos }}</span>
        </div>
      </section>

      <section class="ats">
        <a routerLink="/admin/productos" class="tarjeta ats__card">Gestionar productos</a>
        <a routerLink="/admin/categorias" class="tarjeta ats__card">Gestionar categorías</a>
        <a routerLink="/admin/pedidos" class="tarjeta ats__card">Gestionar pedidos</a>
        <a routerLink="/admin/usuarios" class="tarjeta ats__card">Gestionar usuarios</a>
        <a routerLink="/admin/reportes" class="tarjeta ats__card">Ver reportes</a>
      </section>
    }
  `,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private adminService = inject(AdminService);

  readonly resumen: WritableSignal<ResumenReporte | null> = signal(null);
  readonly cargando = signal(true);

  ngOnInit(): void {
    this.adminService.resumen().subscribe({
      next: (r) => {
        this.resumen.set(r);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }
}