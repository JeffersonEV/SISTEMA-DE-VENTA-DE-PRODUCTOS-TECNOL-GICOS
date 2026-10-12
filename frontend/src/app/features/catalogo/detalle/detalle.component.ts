import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductoService } from '../../../core/services/producto.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { ProductoResponse } from '../../../core/models/models';

@Component({
  selector: 'app-detalle',
  imports: [RouterLink, DecimalPipe],
  template: `
    <div class="back">
      <a routerLink="/catalogo" class="boton boton-ghost boton-sm">← Volver al catálogo</a>
    </div>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando…</div>
    } @else if (producto()) {
      <div class="tarjeta detalle">
        <div class="detalle__info">
          <h1>{{ producto()?.nombre }}</h1>
          <div class="detalle__meta">
            <span class="badge badge-cliente">{{ producto()?.categoriaNombre }}</span>
            <span [class]="'badge ' + (producto()?.activo ? 'badge-activo' : 'badge-inactivo')">
              {{ producto()?.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <p class="detalle__descripcion">{{ producto()?.descripcion }}</p>
          <div class="detalle__precio">S/ {{ producto()?.precio | number: '1.2-2' }}</div>
          <div>
            <span class="detalle__stock" [class.sin-stock]="producto()?.stock === 0">
              {{
                producto()?.stock === 0
                  ? 'Sin stock'
                  : (producto()?.stock ?? 0) <= 5
                    ? '¡Últimas ' + producto()?.stock + ' unidades!'
                    : producto()?.stock + ' unidades disponibles'
              }}
            </span>
          </div>
        </div>
        <div class="detalle__acciones">
          <button class="boton boton-primario boton-lg" [disabled]="producto()?.stock === 0" (click)="agregar()">
            Agregar al carrito
          </button>
          <a routerLink="/carrito" class="boton boton-ghost boton-lg">Ver carrito</a>
        </div>
      </div>
    } @else {
      <div class="vacio">Producto no encontrado.</div>
    }
  `,
  styleUrl: './detalle.component.scss'
})
export class DetalleComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productoService = inject(ProductoService);
  private carritoService = inject(CarritoService);

  readonly producto: WritableSignal<ProductoResponse | null> = signal(null);
  readonly cargando = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productoService.obtener(id).subscribe({
      next: (p) => {
        this.producto.set(p);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.router.navigate(['/catalogo']);
      }
    });
  }

  agregar(): void {
    const p = this.producto();
    if (p) this.carritoService.agregar(p);
  }
}