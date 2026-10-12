import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { CategoriaResponse, PageResponse, ProductoRequest, ProductoResponse } from '../../../core/models/models';

@Component({
  selector: 'app-productos',
  imports: [ReactiveFormsModule, DecimalPipe],
  template: `
    <h1 class="titulo-pagina">Productos</h1>
    <p class="subtitulo-pagina">CRUD completo del inventario de TechStore.</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }

    <div class="admin">
      <section>
        <div class="tabla-contenedor">
          <table class="tabla">
            <thead>
              <tr>
                <th>ID</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (p of productos(); track p.id) {
                <tr>
                  <td>{{ p.id }}</td>
                  <td>{{ p.nombre }}</td>
                  <td>{{ p.categoriaNombre }}</td>
                  <td>S/ {{ p.precio | number: '1.2-2' }}</td>
                  <td>{{ p.stock }}</td>
                  <td>
                    <span [class]="'badge ' + (p.activo ? 'badge-activo' : 'badge-inactivo')">
                      {{ p.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </td>
                  <td>
                    <button class="boton boton-ghost boton-sm" (click)="editar(p)">Editar</button>
                    <button class="boton boton-peligro boton-sm" (click)="eliminar(p)">Eliminar</button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="7" class="vacio">No hay productos.</td></tr>
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
      </section>

      <aside class="tarjeta admin__form">
        <h3>{{ form.controls['id'].value ? 'Editar producto' : 'Nuevo producto' }}</h3>
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-grupo">
            <label>Nombre</label>
            <input formControlName="nombre" />
            @if (form.controls['nombre'].invalid && form.controls['nombre'].touched) {
              <div class="form-error">Obligatorio.</div>
            }
          </div>
          <div class="form-grupo">
            <label>Descripción</label>
            <textarea formControlName="descripcion"></textarea>
          </div>
          <div class="form-grupo">
            <label>Categoría</label>
            <select formControlName="categoriaId">
              <option [ngValue]="null" disabled>Selecciona…</option>
              @for (c of categorias(); track c.id) {
                <option [ngValue]="c.id">{{ c.nombre }}</option>
              }
            </select>
            @if (form.controls['categoriaId'].invalid && form.controls['categoriaId'].touched) {
              <div class="form-error">Selecciona una categoría.</div>
            }
          </div>
          <div class="admin__dos">
            <div class="form-grupo">
              <label>Precio (S/)</label>
              <input type="number" min="0" step="0.01" formControlName="precio" />
              @if (form.controls['precio'].invalid && form.controls['precio'].touched) {
                <div class="form-error">Precio mayor a 0.</div>
              }
            </div>
            <div class="form-grupo">
              <label>Stock</label>
              <input type="number" min="0" formControlName="stock" />
              @if (form.controls['stock'].invalid && form.controls['stock'].touched) {
                <div class="form-error">Stock no negativo.</div>
              }
            </div>
          </div>
          <div class="form-grupo">
            <label>
              <input type="checkbox" formControlName="activo" style="width:auto" />
              Producto activo
            </label>
          </div>
          <div style="display:flex;gap:8px">
            <button class="boton boton-primario" type="submit" [disabled]="form.invalid || guardando()">
              {{ guardando() ? 'Guardando…' : 'Guardar' }}
            </button>
            @if (form.controls['id'].value) {
              <button class="boton boton-ghost" type="button" (click)="nuevo()">Cancelar</button>
            }
          </div>
        </form>
      </aside>
    </div>
  `,
  styleUrl: '../categorias/categorias.component.scss'
})
export class ProductosComponent implements OnInit {
  private productoService = inject(ProductoService);
  private categoriaService = inject(CategoriaService);
  private fb = inject(FormBuilder);

  readonly productos: WritableSignal<ProductoResponse[]> = signal([]);
  readonly categorias: WritableSignal<CategoriaResponse[]> = signal([]);
  readonly pagina: WritableSignal<PageResponse<ProductoResponse> | null> = signal(null);
  readonly actual = signal(0);
  readonly guardando = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    id: [null as number | null],
    nombre: ['', Validators.required],
    descripcion: [''],
    categoriaId: [null as number | null, Validators.required],
    precio: [0 as number | null, [Validators.required, Validators.min(0.01)]],
    stock: [0 as number | null, [Validators.required, Validators.min(0)]],
    activo: [true]
  });

  ngOnInit(): void {
    this.categoriaService.listar().subscribe((cats) => this.categorias.set(cats));
    this.cargar();
  }

  cargar(): void {
    this.productoService.listar({ pagina: this.actual(), tamanio: 10 }).subscribe({
      next: (res) => {
        this.productos.set(res.content);
        this.pagina.set(res);
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'Error al cargar productos.')
    });
  }

  cambiarPagina(p: number): void {
    this.actual.set(p);
    this.cargar();
  }

  nuevo(): void {
    this.form.reset({ id: null, nombre: '', descripcion: '', categoriaId: null, precio: 0, stock: 0, activo: true });
  }

  editar(p: ProductoResponse): void {
    this.form.setValue({
      id: p.id,
      nombre: p.nombre,
      descripcion: p.descripcion,
      categoriaId: p.categoriaId,
      precio: p.precio,
      stock: p.stock,
      activo: p.activo
    });
  }

  guardar(): void {
    if (this.form.invalid) return;
    const { id, activo, ...resto } = this.form.getRawValue();
    const request: ProductoRequest = {
      nombre: resto.nombre,
      descripcion: resto.descripcion ?? '',
      precio: resto.precio!,
      stock: resto.stock!,
      categoriaId: resto.categoriaId!,
      activo: activo!
    };
    this.guardando.set(true);
    this.error.set('');
    const operacion = id
      ? this.productoService.actualizar(id, request)
      : this.productoService.crear(request);
    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.nuevo();
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el producto.');
      }
    });
  }

  eliminar(p: ProductoResponse): void {
    if (!confirm(`¿Eliminar el producto "${p.nombre}"?`)) return;
    this.productoService.eliminar(p.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar.')
    });
  }
}