import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import {
  ProductoStockBajo,
  ResumenReporte,
  TopProducto,
  VentasPorCategoria
} from '../../../core/models/models';

@Component({
  selector: 'app-reportes',
  imports: [DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Reportes de ventas</h1>
    <p class="subtitulo-pagina">Indicadores generados con consultas JPQL agregadas.</p>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Generando reportes…</div>
    } @else {
      @if (resumen()) {
        <section class="kpis">
          <div class="tarjeta kpi">
            <span class="kpi__label">Total ventas</span>
            <span class="kpi__valor">S/ {{ resumen()!.totalVentas | number: '1.2-2' }}</span>
          </div>
          <div class="tarjeta kpi">
            <span class="kpi__label">Total pedidos</span>
            <span class="kpi__valor">{{ resumen()!.totalPedidos }}</span>
          </div>
          <div class="tarjeta kpi kpi-pendiente">
            <span class="kpi__label">Pendientes</span>
            <span class="kpi__valor">{{ resumen()!.pedidosPendientes }}</span>
          </div>
          <div class="tarjeta kpi kpi-pagado">
            <span class="kpi__label">Pagados</span>
            <span class="kpi__valor">{{ resumen()!.pedidosPagados }}</span>
          </div>
          <div class="tarjeta kpi kpi-cancelado">
            <span class="kpi__label">Cancelados</span>
            <span class="kpi__valor">{{ resumen()!.pedidosCancelados }}</span>
          </div>
        </section>
      }

      <div class="reportes">
        <div class="tabla-contenedor">
          <h3 class="reportes__titulo">Top productos</h3>
          <table class="tabla">
            <thead>
              <tr><th>#</th><th>Producto</th><th>Cant. vendida</th><th>Total</th></tr>
            </thead>
            <tbody>
              @for (t of topProductos(); track t.productoId; let i = $index) {
                <tr>
                  <td>{{ i + 1 }}</td>
                  <td>{{ t.nombre }}</td>
                  <td>{{ t.cantidadVendida }}</td>
                  <td>S/ {{ t.totalVendido | number: '1.2-2' }}</td>
                </tr>
              } @empty {
                <tr><td colspan="4" class="vacio">Sin datos.</td></tr>
              }
            </tbody>
          </table>
        </div>

        <div class="tabla-contenedor">
          <h3 class="reportes__titulo">Ventas por categoría</h3>
          <table class="tabla">
            <thead>
              <tr><th>Categoría</th><th>Cant. vendida</th><th>Total</th></tr>
            </thead>
            <tbody>
              @for (v of ventasPorCategoria(); track v.categoria) {
                <tr>
                  <td>{{ v.categoria }}</td>
                  <td>{{ v.cantidadVendida }}</td>
                  <td>S/ {{ v.totalVendido | number: '1.2-2' }}</td>
                </tr>
              } @empty {
                <tr><td colspan="3" class="vacio">Sin datos.</td></tr>
              }
            </tbody>
          </table>
        </div>

        <div class="tabla-contenedor">
          <h3 class="reportes__titulo">Stock bajo (reposición)</h3>
          <table class="tabla">
            <thead>
              <tr><th>Producto</th><th>Stock</th></tr>
            </thead>
            <tbody>
              @for (s of stockBajo(); track s.id) {
                <tr>
                  <td>{{ s.nombre }}</td>
                  <td><span class="badge badge-cancelado">{{ s.stock }}</span></td>
                </tr>
              } @empty {
                <tr><td colspan="2" class="vacio">Todo el inventario está por encima del umbral.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
  styleUrl: './reportes.component.scss'
})
export class ReportesComponent implements OnInit {
  private adminService = inject(AdminService);

  readonly resumen: WritableSignal<ResumenReporte | null> = signal(null);
  readonly topProductos: WritableSignal<TopProducto[]> = signal([]);
  readonly ventasPorCategoria: WritableSignal<VentasPorCategoria[]> = signal([]);
  readonly stockBajo: WritableSignal<ProductoStockBajo[]> = signal([]);
  readonly cargando = signal(true);

  ngOnInit(): void {
    let pendientes = 4;
    const fin = () => {
      pendientes--;
      if (pendientes === 0) this.cargando.set(false);
    };
    this.adminService.resumen().subscribe({ next: (r) => (this.resumen.set(r), fin()), error: fin });
    this.adminService.topProductos(5).subscribe({ next: (t) => (this.topProductos.set(t), fin()), error: fin });
    this.adminService.ventasPorCategoria().subscribe({ next: (v) => (this.ventasPorCategoria.set(v), fin()), error: fin });
    this.adminService.stockBajo(5).subscribe({ next: (s) => (this.stockBajo.set(s), fin()), error: fin });
  }
}