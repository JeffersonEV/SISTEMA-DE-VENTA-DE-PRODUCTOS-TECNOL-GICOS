import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PedidoService } from '../../core/services/pedido.service';
import { PedidoResponse } from '../../core/models/models';

@Component({
  selector: 'app-mis-pedidos',
  imports: [RouterLink, DecimalPipe, DatePipe],
  template: `
    <h1 class="titulo-pagina">Mis pedidos</h1>
    <p class="subtitulo-pagina">Historial de tus compras en TechStore.</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando pedidos…</div>
    } @else if (pedidos().length === 0) {
      <div class="vacio">
        <p>Aún no has realizado pedidos.</p>
        <a routerLink="/catalogo" class="boton boton-primario" style="margin-top: 14px">Ver catálogo</a>
      </div>
    } @else {
      <div class="lista">
        @for (pedido of pedidos(); track pedido.id; let idx = $index) {
          <article class="tarjeta pedido">
            <div class="pedido__cabecera">
              <div>
                <strong>Pedido #{{ pedido.id }}</strong>
                <span class="pedido__fecha">· {{ pedido.fecha | date: 'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <span [class]="'badge badge-' + (pedido.estado === 'PENDIENTE' ? 'pendiente' : pedido.estado === 'PAGADO' ? 'pagado' : 'cancelado')">
                {{ pedido.estado }}
              </span>
            </div>

            <table class="pedido__detalles">
              <tbody>
                @for (d of pedido.detalles; track d.id) {
                  <tr>
                    <td>{{ d.productoNombre }}</td>
                    <td class="pedido__cantidad">x{{ d.cantidad }}</td>
                    <td class="pedido__derecha">S/ {{ d.subtotal | number: '1.2-2' }}</td>
                  </tr>
                }
              </tbody>
            </table>

            <div class="pedido__pie">
              <strong>Total: S/ {{ pedido.total | number: '1.2-2' }}</strong>
              @if (pedido.estado === 'PENDIENTE') {
                <button class="boton boton-peligro boton-sm" [disabled]="cancelando() === pedido.id" (click)="cancelar(pedido)">
                  {{ cancelando() === pedido.id ? 'Cancelando…' : 'Cancelar pedido' }}
                </button>
              }
            </div>
          </article>
        }
      </div>
    }
  `,
  styleUrl: './mis-pedidos.component.scss'
})
export class MisPedidosComponent implements OnInit {
  private pedidoService = inject(PedidoService);

  readonly pedidos: WritableSignal<PedidoResponse[]> = signal([]);
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly cancelando = signal<number | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.pedidoService.misPedidos().subscribe({
      next: (p) => {
        this.pedidos.set(p);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  cancelar(pedido: PedidoResponse): void {
    if (!confirm(`¿Cancelar el pedido #${pedido.id}? Se restaurará el stock.`)) return;
    this.cancelando.set(pedido.id);
    this.error.set('');
    this.pedidoService.cancelar(pedido.id).subscribe({
      next: (actualizado) => {
        this.cancelando.set(null);
        this.pedidos.set(this.pedidos().map((p) => (p.id === actualizado.id ? actualizado : p)));
      },
      error: (err) => {
        this.cancelando.set(null);
        this.error.set(err.error?.mensaje ?? 'No se pudo cancelar el pedido.');
      }
    });
  }
}