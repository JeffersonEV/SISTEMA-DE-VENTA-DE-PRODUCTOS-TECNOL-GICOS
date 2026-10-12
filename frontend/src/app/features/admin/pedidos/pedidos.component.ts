import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { EstadoPedido, PageResponse, PedidoResponse } from '../../../core/models/models';

@Component({
  selector: 'app-pedidos',
  imports: [ReactiveFormsModule, DecimalPipe, DatePipe],
  template: `
    <h1 class="titulo-pagina">Gestión de pedidos</h1>
    <p class="subtitulo-pagina">Consulta, filtra y actualiza el estado de los pedidos.</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }

    <form class="filtros tarjeta" [formGroup]="form" (ngSubmit)="buscar()">
      <div class="form-grupo">
        <label>Estado</label>
        <select formControlName="estado">
          <option value="">Todos</option>
          @for (e of estados; track e) {
            <option [value]="e">{{ e }}</option>
          }
        </select>
      </div>
      <div class="form-grupo">
        <label>ID de usuario</label>
        <input type="number" min="1" formControlName="usuarioId" placeholder="Ej: 2" />
      </div>
      <div class="form-grupo">
        <label>Desde</label>
        <input type="date" formControlName="desde" />
      </div>
      <div class="form-grupo">
        <label>Hasta</label>
        <input type="date" formControlName="hasta" />
      </div>
      <div class="filtros__acciones">
        <button class="boton boton-primario" type="submit">Filtrar</button>
        <button class="boton boton-ghost" type="button" (click)="limpiar()">Limpiar</button>
      </div>
    </form>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando…</div>
    } @else {
      <div class="tabla-contenedor">
        <table class="tabla">
          <thead>
            <tr>
              <th>ID</th><th>Cliente</th><th>Fecha</th><th>Total</th><th>Estado</th><th>Cambiar estado</th>
            </tr>
          </thead>
          <tbody>
            @for (pedido of pedidos(); track pedido.id) {
              <tr>
                <td>#{{ pedido.id }}</td>
                <td>{{ pedido.usuarioNombre }}</td>
                <td>{{ pedido.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                <td>S/ {{ pedido.total | number: '1.2-2' }}</td>
                <td>
                  <span [class]="'badge badge-' + (pedido.estado === 'PENDIENTE' ? 'pendiente' : pedido.estado === 'PAGADO' ? 'pagado' : 'cancelado')">
                    {{ pedido.estado }}
                  </span>
                </td>
                <td>
                  <select
                    class="pedidos__select"
                    [value]="pedido.estado"
                    [disabled]="pedido.estado === 'CANCELADO' || cambiando() === pedido.id"
                    (change)="cambiarEstado(pedido, $any($event.target).value)">
                    @for (e of estados; track e) {
                      <option [value]="e">{{ e }}</option>
                    }
                  </select>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="6" class="vacio">No hay pedidos con esos filtros.</td></tr>
            }
          </tbody>
        </table>
      </div>

      @if (pagina() && pagina()!.totalPaginas > 1) {
        <div class="paginacion">
          <button class="boton boton-ghost boton-sm" [disabled]="actual() === 0" (click)="cambiarPagina(actual() - 1)">‹ Anterior</button>
          <span class="pag-info">Página {{ actual() + 1 }} de {{ pagina()!.totalPaginas }}</span>
          <button class="boton boton-ghost boton-sm" [disabled]="pagina()!.ultima" (click)="cambiarPagina(actual() + 1)">Siguiente ›</button>
        </div>
      }
    }
  `,
  styleUrl: './pedidos.component.scss'
})
export class PedidosComponent implements OnInit {
  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);

  readonly estados: EstadoPedido[] = ['PENDIENTE', 'PAGADO', 'CANCELADO'];
  readonly pedidos: WritableSignal<PedidoResponse[]> = signal([]);
  readonly pagina: WritableSignal<PageResponse<PedidoResponse> | null> = signal(null);
  readonly actual = signal(0);
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly cambiando = signal<number | null>(null);

  form = this.fb.nonNullable.group({
    estado: [''],
    usuarioId: [null as number | null],
    desde: [''],
    hasta: ['']
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    const f = this.form.getRawValue();
    this.adminService
      .listarPedidos({
        pagina: this.actual(),
        estado: (f.estado || undefined) as EstadoPedido | undefined,
        usuarioId: f.usuarioId ?? undefined,
        desde: f.desde || undefined,
        hasta: f.hasta || undefined
      })
      .subscribe({
        next: (res) => {
          this.pedidos.set(res.content);
          this.pagina.set(res);
          this.cargando.set(false);
        },
        error: () => this.cargando.set(false)
      });
  }

  buscar(): void {
    this.actual.set(0);
    this.cargar();
  }

  limpiar(): void {
    this.form.reset();
    this.buscar();
  }

  cambiarPagina(p: number): void {
    this.actual.set(p);
    this.cargar();
  }

  cambiarEstado(pedido: PedidoResponse, estado: EstadoPedido): void {
    if (pedido.estado === estado) return;
    this.cambiando.set(pedido.id);
    this.error.set('');
    this.adminService.cambiarEstado(pedido.id, estado).subscribe({
      next: (actualizado) => {
        this.cambiando.set(null);
        this.pedidos.set(this.pedidos().map((p) => (p.id === actualizado.id ? actualizado : p)));
      },
      error: (err) => {
        this.cambiando.set(null);
        this.error.set(err.error?.mensaje ?? 'No se pudo actualizar el estado.');
        this.cargar();
      }
    });
  }
}