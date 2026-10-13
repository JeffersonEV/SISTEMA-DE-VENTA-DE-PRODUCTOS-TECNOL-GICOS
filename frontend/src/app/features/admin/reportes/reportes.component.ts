import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import {
  ProductoStockBajo,
  ResumenReporte,
  TopProducto,
  VentasPorCategoria
} from '../../../core/models/models';
import { colorGrafica, fondoDonut, porcentajeBarra } from '../../../core/utils/graficos';

interface EstadoGrafica {
  etiqueta: string;
  valor: number;
  color: string;
}

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
        <section class="kpis kpis--reportes">
          <div class="tarjeta kpi kpi--grande">
            <span class="kpi__label">Total ventas</span>
            <span class="kpi__valor">S/ {{ resumen()!.totalVentas | number: '1.2-2' }}</span>
            <span class="kpi__sub">
              {{ resumen()!.totalPedidos }} pedidos · ticket promedio S/ {{ ticketPromedio() | number: '1.2-2' }}
            </span>
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

      <div class="doble-columna">
        <section class="grafico-seccion">
          <h3 class="grafico-seccion__titulo">Top productos por monto</h3>
          <p class="grafico-seccion__sub">Productos con mayor recaudación.</p>
          <div class="grafico-barras">
            @for (t of topProductos(); track t.productoId) {
              <div class="barra">
                <span class="barra__label" [title]="t.nombre">{{ t.nombre }}</span>
                <div class="barra__pista">
                  <div class="barra__relleno" [style.width.%]="porcentaje(t.totalVendido, maxTotalVendido())"></div>
                </div>
                <span class="barra__valor">S/ {{ t.totalVendido | number: '1.2-2' }}</span>
              </div>
            } @empty {
              <div class="vacio">Sin datos.</div>
            }
          </div>
        </section>

        <section class="grafico-seccion">
          <h3 class="grafico-seccion__titulo">Ventas por categoría</h3>
          <p class="grafico-seccion__sub">Distribución del monto vendido.</p>
          @if (ventasPorCategoria().length) {
            <div class="donut" [style.background]="fondoCategorias()">
              <div class="donut__centro">
                <span class="donut__label">Total</span>
                <span class="donut__total">S/ {{ totalCategorias() | number: '1.0-0' }}</span>
              </div>
            </div>
            <ul class="leyenda">
              @for (v of ventasPorCategoria(); track v.categoria; let i = $index) {
                <li>
                  <span class="leyenda__punto" [style.background]="color(i)"></span>
                  {{ v.categoria }}
                  <b>S/ {{ v.totalVendido | number: '1.2-2' }}</b>
                </li>
              }
            </ul>
          } @else {
            <div class="vacio">Sin datos.</div>
          }
        </section>
      </div>

      <div class="doble-columna">
        <section class="grafico-seccion">
          <h3 class="grafico-seccion__titulo">Estado de los pedidos</h3>
          <p class="grafico-seccion__sub">Pedidos agrupados por estado.</p>
          <div class="donut" [style.background]="fondoEstados()">
            <div class="donut__centro">
              <span class="donut__label">Pedidos</span>
              <span class="donut__total">{{ resumen()?.totalPedidos ?? 0 }}</span>
            </div>
          </div>
          <ul class="leyenda">
            @for (e of estados(); track e.etiqueta) {
              <li>
                <span class="leyenda__punto" [style.background]="e.color"></span>
                {{ e.etiqueta }}
                <b>{{ e.valor }}</b>
              </li>
            }
          </ul>
        </section>

        <section class="grafico-seccion">
          <h3 class="grafico-seccion__titulo">Unidades vendidas</h3>
          <p class="grafico-seccion__sub">Cantidad vendida por producto.</p>
          <div class="grafico-barras">
            @for (t of topProductos(); track t.productoId) {
              <div class="barra">
                <span class="barra__label" [title]="t.nombre">{{ t.nombre }}</span>
                <div class="barra__pista">
                  <div
                    class="barra__relleno"
                    [style.width.%]="porcentaje(t.cantidadVendida, maxCantidadVendida())"
                    style="background: linear-gradient(90deg, #0891b2, #16a34a)"
                  ></div>
                </div>
                <span class="barra__valor">{{ t.cantidadVendida }} u.</span>
              </div>
            } @empty {
              <div class="vacio">Sin datos.</div>
            }
          </div>
        </section>
      </div>

      <section class="tabla-contenedor">
        <h3 class="reportes__titulo">Stock bajo (reposición)</h3>
        <table class="tabla">
          <thead>
            <tr><th>Producto</th><th>Stock</th><th>Nivel</th></tr>
          </thead>
          <tbody>
            @for (s of stockBajo(); track s.id) {
              <tr>
                <td>{{ s.nombre }}</td>
                <td><span class="badge badge-cancelado">{{ s.stock }}</span></td>
                <td>
                  <div class="barra__pista" style="max-width: 220px">
                    <div
                      class="barra__relleno"
                      [style.width.%]="porcentaje(s.stock, umbralStock())"
                      style="background: linear-gradient(90deg, #d97706, #dc2626)"
                    ></div>
                  </div>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="3" class="vacio">Todo el inventario está por encima del umbral.</td></tr>
            }
          </tbody>
        </table>
      </section>
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

  private readonly umbral = 5;

  ngOnInit(): void {
    let pendientes = 4;
    const listo = () => {
      if (--pendientes === 0) {
        this.cargando.set(false);
      }
    };

    this.adminService.resumen().subscribe({
      next: (r) => {
        this.resumen.set(r);
        listo();
      },
      error: () => listo()
    });
    this.adminService.topProductos(5).subscribe({
      next: (t) => {
        this.topProductos.set(t);
        listo();
      },
      error: () => listo()
    });
    this.adminService.ventasPorCategoria().subscribe({
      next: (v) => {
        this.ventasPorCategoria.set(v);
        listo();
      },
      error: () => listo()
    });
    this.adminService.stockBajo(this.umbral).subscribe({
      next: (s) => {
        this.stockBajo.set(s);
        listo();
      },
      error: () => listo()
    });
  }

  porcentaje(valor: number, maximo: number): number {
    return porcentajeBarra(valor, maximo);
  }

  color(indice: number): string {
    return colorGrafica(indice);
  }

  umbralStock(): number {
    return this.umbral;
  }

  maxTotalVendido(): number {
    return Math.max(...this.topProductos().map((t) => t.totalVendido), 0);
  }

  maxCantidadVendida(): number {
    return Math.max(...this.topProductos().map((t) => t.cantidadVendida), 0);
  }

  totalCategorias(): number {
    return this.ventasPorCategoria().reduce((total, v) => total + v.totalVendido, 0);
  }

  ticketPromedio(): number {
    const r = this.resumen();
    if (!r || r.totalPedidos <= 0) {
      return 0;
    }
    return r.totalVentas / r.totalPedidos;
  }

  fondoCategorias(): string {
    return fondoDonut(this.ventasPorCategoria().map((v) => ({ valor: v.totalVendido })));
  }

  estados(): EstadoGrafica[] {
    const r = this.resumen();
    return [
      { etiqueta: 'Pagados', valor: r?.pedidosPagados ?? 0, color: '#16a34a' },
      { etiqueta: 'Pendientes', valor: r?.pedidosPendientes ?? 0, color: '#d97706' },
      { etiqueta: 'Cancelados', valor: r?.pedidosCancelados ?? 0, color: '#dc2626' }
    ];
  }

  fondoEstados(): string {
    return fondoDonut(this.estados().map((e) => ({ valor: e.valor })));
  }
}
