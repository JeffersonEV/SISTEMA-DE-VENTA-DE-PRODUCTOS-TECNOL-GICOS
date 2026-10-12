import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService, CategoriaRequest } from '../../../core/services/categoria.service';
import { CategoriaResponse } from '../../../core/models/models';

@Component({
  selector: 'app-categorias',
  imports: [ReactiveFormsModule],
  template: `
    <h1 class="titulo-pagina">Categorías</h1>
    <p class="subtitulo-pagina">Administra las categorías del catálogo.</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }

    <div class="admin">
      <section>
        <div class="tabla-contenedor">
          <table class="tabla">
            <thead>
              <tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              @for (c of categorias(); track c.id) {
                <tr>
                  <td>{{ c.id }}</td>
                  <td>{{ c.nombre }}</td>
                  <td>{{ c.descripcion }}</td>
                  <td>
                    <button class="boton boton-ghost boton-sm" (click)="editar(c)">Editar</button>
                    <button class="boton boton-peligro boton-sm" (click)="eliminar(c)">Eliminar</button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="4" class="vacio">No hay categorías.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <aside class="tarjeta admin__form">
        <h3>{{ form.controls['id'].value ? 'Editar categoría' : 'Nueva categoría' }}</h3>
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-grupo">
            <label>Nombre</label>
            <input formControlName="nombre" placeholder="Ej: Laptops" />
            @if (form.controls['nombre'].invalid && form.controls['nombre'].touched) {
              <div class="form-error">El nombre es obligatorio.</div>
            }
          </div>
          <div class="form-grupo">
            <label>Descripción</label>
            <textarea formControlName="descripcion" placeholder="Descripción breve…"></textarea>
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
  styleUrl: './categorias.component.scss'
})
export class CategoriasComponent implements OnInit {
  private categoriaService = inject(CategoriaService);
  private fb = inject(FormBuilder);

  readonly categorias: WritableSignal<CategoriaResponse[]> = signal([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    id: [null as number | null],
    nombre: ['', Validators.required],
    descripcion: ['']
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.categoriaService.listar().subscribe((cats) => {
      this.categorias.set(cats);
      this.cargando.set(false);
    });
  }

  nuevo(): void {
    this.form.reset({ id: null, nombre: '', descripcion: '' });
  }

  editar(c: CategoriaResponse): void {
    this.form.setValue({ id: c.id, nombre: c.nombre, descripcion: c.descripcion });
  }

  guardar(): void {
    if (this.form.invalid) return;
    const { id, ...categoria } = this.form.getRawValue();
    const request: CategoriaRequest = { nombre: categoria.nombre, descripcion: categoria.descripcion ?? '' };
    this.guardando.set(true);
    this.error.set('');
    const operacion = id
      ? this.categoriaService.actualizar(id, request)
      : this.categoriaService.crear(request);
    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.nuevo();
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar la categoría.');
      }
    });
  }

  eliminar(c: CategoriaResponse): void {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) return;
    this.categoriaService.eliminar(c.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar.')
    });
  }
}