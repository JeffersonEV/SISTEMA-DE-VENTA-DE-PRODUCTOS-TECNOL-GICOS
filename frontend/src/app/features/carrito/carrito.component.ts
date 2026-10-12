import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../core/services/carrito.service';
import { PedidoService } from '../../core/services/pedido.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-carrito',
  imports: [RouterLink, DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Tu carrito</h1>
    <p class="subtitulo-pagina">{{ carrito.total() > 0 ? 'Revisa tu selección antes de confirmar.' : 'Tu carrito está vacío.' }}</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }
    @if (confirmado()) {
      <div class="mensaje-exito">¡Pedido realizado con éxito! N.º {{ confirmado() }}</div>
    }

    @if (carrito.items().length === 0 && !confirmado()) {
      <div class="vacio">
        <p>No tienes productos agregados.</p>
        <a routerLink="/catalogo" class="boton boton-primario" style="margin-top: 14px">Ir al catálogo</a>
      </div>
    } @else {
      <div class="carrito">
        <div class="carrito__lista">
          @for (item of carrito.items(); track item.producto.id) {
            <article class="tarjeta carrito__item">
              <div class="carrito__info">
                <a [routerLink]="['/catalogo', item.producto.id]" class="carrito__nombre">{{ item.producto.nombre }}</a>
                <div class="carrito__precio">S/ {{ item.producto.precio | number: '1.2-2' }}</div>
                <div class="carrito__stock">Stock: {{ item.producto.stock }}</div>
              </div>
              <div class="carrito__cantidad">
                <button class="boton boton-ghost boton-sm" (click)="restar(item.producto.id)">−</button>
                <span class="carrito__num">{{ item.cantidad }}</span>
                <button class="boton boton-ghost boton-sm" [disabled]="item.cantidad >= item.producto.stock" (click)="sumar(item.producto.id)">+</button>
              </div>
              <div class="carrito__subtotal">S/ {{ item.cantidad * item.producto.precio | number: '1.2-2' }}</div>
              <button class="boton boton-peligro boton-sm" (click)="carrito.quitar(item.producto.id)">Quitar</button>
            </article>
          }
        </div>

        @if (carrito.items().length > 0) {
          <aside class="tarjeta carrito__resumen">
            <h3>Resumen</h3>
            <div class="carrito__resumen-fila">
              <span>Productos</span><span>{{ carrito.cantidadTotal() }}</span>
            </div>
            <div class="carrito__resumen-fila carrito__resumen-total">
              <span>Total</span><span>S/ {{ carrito.total() | number: '1.2-2' }}</span>
            </div>
            <button class="boton boton-exito" [disabled]="enviando()" (click)="comprar()">
              {{ enviando() ? 'Confirmando…' : 'Realizar pedido' }}
            </button>
            <a routerLink="/catalogo" class="boton boton-ghost">Seguir comprando</a>
          </aside>
        }
      </div>
    }
  `,
  styleUrl: './carrito.component.scss'
})
export class CarritoComponent {
  readonly carrito = inject(CarritoService);
  private pedidoService = inject(PedidoService);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly enviando = signal(false);
  readonly error = signal('');
  readonly confirmado = signal<number | null>(null);

  sumar(id: number): void {
    const item = this.carrito.items().find((i) => i.producto.id === id);
    if (item) this.carrito.cambiarCantidad(id, item.cantidad + 1);
  }

  restar(id: number): void {
    const item = this.carrito.items().find((i) => i.producto.id === id);
    if (item) this.carrito.cambiarCantidad(id, item.cantidad - 1);
  }

  comprar(): void {
    if (!this.auth.isLogueado()) {
      this.router.navigate(['/login'], { queryParams: { redirect: '/carrito' } });
      return;
    }
    this.error.set('');
    this.enviando.set(true);
    const items = this.carrito.items().map((i) => ({ productoId: i.producto.id, cantidad: i.cantidad }));
    this.pedidoService.crear(items).subscribe({
      next: (pedido) => {
        this.enviando.set(false);
        this.carrito.vaciar();
        this.confirmado.set(pedido.id);
      },
      error: (err) => {
        this.enviando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo realizar el pedido.');
      }
    });
  }
}