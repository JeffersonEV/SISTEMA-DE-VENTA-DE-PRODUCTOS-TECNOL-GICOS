import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { ResumenReporte, TopProducto, VentasPorCategoria } from '../../../core/models/models';
import { colorGrafica, fondoDonut, porcentajeBarra } from '../../../core/utils/graficos';

interface EstadoGrafica {
  etiqueta: string;
  valor: number;
  color: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Panel de administración</h1>
    <p class="subtitulo-pagina">Resumen general de la tienda.</p>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando…</div>
    } @else if (resumen()) {
      <section class="hero-ventas">
        <div class="hero-ventas__texto">
          <span class="hero-ventas__etiqueta">Total ventas</span>
          <span class="hero-ventas__valor">S/ {{ resumen()!.totalVentas | number: '1.2-2' }}</span>
          <span class="hero-ventas__sub">
            {{ resumen()!.pedidosPagados }} pagados · {{ resumen()!.pedidosPendientes }} pendientes · {{ resumen()!.pedidosCancelados }} cancelados
          </span>
        </div>
        <div class="hero-ventas__icono">&#128176;</div>
      </section>

      <section class="kpis">
        <div class="tarjeta kpi">
          <span class="kpi__label">Pedidos</span>
          <span class="kpi__valor">{{ resumen()!.totalPedidos }}</span>
          <span class="kpi__sub">{{ resumen()!.pedidosPendientes }} pendientes · {{ resumen()!.pedidosPagados }} pagados</span>
        </div>
        <div class="tarjeta kpi">
          <span class="kpi__label">Usuarios</span>
          <span class="kpi__valor">{{ resumen()!.totalUsuarios }}</span>
          <span class="kpi__sub">registrados en la tienda</span>
        </div>
        <div class="tarjeta kpi">
          <span class="kpi__label">Productos</span>
          <span class="kpi__valor">{{ resumen()!.totalProductos }}</span>
          <span class="kpi__sub">en el catálogo</span>
        </div>
      </section>

      <div class="doble-columna">
        <section class="grafico-seccion">
          <h3 class="grafico-seccion__titulo">Top productos</h3>
          <p class="grafico-seccion__sub">Productos con mayor monto vendido.</p>
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
              <div class="vacio">Sin ventas registradas.</div>
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
            <div class="vacio">Sin ventas por categoría.</div>
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
              <span class="donut__total">{{ resumen()!.totalPedidos }}</span>
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
              <div class="vacio">Sin ventas registradas.</div>
            }
          </div>
        </section>
      </div>

      <section class="ats">
        <a routerLink="/admin/productos" class="tarjeta ats__card">
          <span class="ats__ico">&#128230;</span> Productos
        </a>
        <a routerLink="/admin/categorias" class="tarjeta ats__card">
          <span class="ats__ico">&#127991;&#65039;</span> Categorías
        </a>
        <a routerLink="/admin/pedidos" class="tarjeta ats__card">
          <span class="ats__ico">&#128203;</span> Pedidos
        </a>
        <a routerLink="/admin/usuarios" class="tarjeta ats__card">
          <span class="ats__ico">&#128101;</span> Usuarios
        </a>
        <a routerLink="/admin/reportes" class="tarjeta ats__card">
          <span class="ats__ico">&#128200;</span> Reportes
        </a>
      </section>
    }
  `,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private adminService = inject(AdminService);

  readonly resumen: WritableSignal<ResumenReporte | null> = signal(null);
  readonly topProductos: WritableSignal<TopProducto[]> = signal([]);
  readonly ventasPorCategoria: WritableSignal<VentasPorCategoria[]> = signal([]);
  readonly cargando = signal(true);

  ngOnInit(): void {
    let pendientes = 3;
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
  }

  porcentaje(valor: number, maximo: number): number {
    return porcentajeBarra(valor, maximo);
  }

  color(indice: number): string {
    return colorGrafica(indice);
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
