import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../core/services/producto.service';
import { CategoriaService } from '../../core/services/categoria.service';
import { CarritoService } from '../../core/services/carrito.service';
import { CategoriaResponse, PageResponse, ProductoResponse } from '../../core/models/models';

@Component({
  selector: 'app-catalogo',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Catálogo de productos</h1>
    <p class="subtitulo-pagina">Encuentra componentes, computadoras y accesorios.</p>

    <form class="filtros tarjeta" [formGroup]="form" (ngSubmit)="buscar()">
      <div class="form-grupo">
        <label>Buscar</label>
        <input formControlName="nombre" placeholder="Nombre del producto…" />
      </div>
      <div class="form-grupo">
        <label>Categoría</label>
        <select formControlName="categoriaId">
          <option value="">Todas</option>
          @for (c of categorias(); track c.id) {
            <option [value]="c.id">{{ c.nombre }}</option>
          }
        </select>
      </div>
      <div class="form-grupo">
        <label>Precio mínimo</label>
        <input type="number" formControlName="minPrecio" min="0" placeholder="0" />
      </div>
      <div class="form-grupo">
        <label>Precio máximo</label>
        <input type="number" formControlName="maxPrecio" min="0" placeholder="0" />
      </div>
      <div class="filtros__acciones">
        <button class="boton boton-primario" type="submit">Filtrar</button>
        <button class="boton boton-ghost" type="button" (click)="limpiar()">Limpiar</button>
      </div>
    </form>

    @if (cargando()) {
      <div class="cargando"><div class="spinner"></div>Cargando productos…</div>
    } @else if (productos().length === 0) {
      <div class="vacio">No se encontraron productos con esos filtros.</div>
    } @else {
      <section class="grid">
        @for (p of productos(); track p.id) {
          <article class="tarjeta producto">
            <div class="producto__nombre">{{ p.nombre }}</div>
            <p class="producto__descripcion">{{ p.descripcion }}</p>
            <div class="producto__categoria">
              <span class="badge badge-cliente">{{ p.categoriaNombre }}</span>
            </div>
            <div class="producto__precio">S/ {{ p.precio | number: '1.2-2' }}</div>
            <div class="producto__stock"
              [class.sin-stock]="p.stock === 0" [class.stock-bajo]="p.stock > 0 && p.stock <= 5">
              {{ p.stock === 0 ? 'Sin stock' : (p.stock <= 5 ? '¡Solo ' + p.stock + ' disponibles!' : p.stock + ' en stock') }}
            </div>
            <div class="producto__acciones">
              <a [routerLink]="['/catalogo', p.id]" class="boton boton-ghost boton-sm">Ver</a>
              <button class="boton boton-primario boton-sm" [disabled]="p.stock === 0" (click)="agregar(p)">
                Agregar
              </button>
            </div>
          </article>
        }
      </section>

      @if (pagina()!.totalPaginas > 1) {
        <div class="paginacion">
          <button class="boton boton-ghost boton-sm" [disabled]="actual() === 0" (click)="cambiarPagina(actual() - 1)">‹ Anterior</button>
          <span class="pag-info">Página {{ actual() + 1 }} de {{ pagina()!.totalPaginas }}</span>
          <button class="boton boton-ghost boton-sm" [disabled]="pagina()!.ultima" (click)="cambiarPagina(actual() + 1)">Siguiente ›</button>
        </div>
      }
    }
  `,
  styleUrl: './catalogo.component.scss'
})
export class CatalogoComponent implements OnInit {
  private productoService = inject(ProductoService);
  private categoriaService = inject(CategoriaService);
  private carritoService = inject(CarritoService);
  private fb = inject(FormBuilder);

  readonly productos: WritableSignal<ProductoResponse[]> = signal([]);
  readonly categorias: WritableSignal<CategoriaResponse[]> = signal([]);
  readonly pagina: WritableSignal<PageResponse<ProductoResponse> | null> = signal(null);
  readonly actual = signal(0);
  readonly cargando = signal(true);

  form = this.fb.nonNullable.group({
    nombre: [''],
    categoriaId: [''],
    minPrecio: [null as number | null],
    maxPrecio: [null as number | null]
  });

  ngOnInit(): void {
    this.categoriaService.listar().subscribe((cats) => this.categorias.set(cats));
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    const f = this.form.getRawValue();
    this.productoService
      .listar({
        pagina: this.actual(),
        nombre: f.nombre || undefined,
        categoriaId: f.categoriaId ? Number(f.categoriaId) : undefined,
        minPrecio: f.minPrecio ?? undefined,
        maxPrecio: f.maxPrecio ?? undefined
      })
      .subscribe({
        next: (res) => {
          this.productos.set(res.content);
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
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.actual.set(p);
    this.cargar();
  }

  agregar(p: ProductoResponse): void {
    this.carritoService.agregar(p);
  }
}