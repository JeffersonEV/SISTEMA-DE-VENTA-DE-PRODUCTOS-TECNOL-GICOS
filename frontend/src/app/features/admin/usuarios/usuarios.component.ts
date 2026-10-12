import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { PageResponse, Rol, UsuarioRequest, UsuarioResponse, UsuarioUpdateRequest } from '../../../core/models/models';

@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule],
  template: `
    <h1 class="titulo-pagina">Usuarios</h1>
    <p class="subtitulo-pagina">Administra las cuentas, roles y estado de los usuarios.</p>

    @if (error()) {
      <div class="mensaje-error">{{ error() }}</div>
    }
    @if (exito()) {
      <div class="mensaje-exito">{{ exito() }}</div>
    }

    <div class="admin">
      <section>
        <div class="tabla-contenedor">
          <table class="tabla">
            <thead>
              <tr>
                <th>ID</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Activo</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (u of usuarios(); track u.id) {
                <tr>
                  <td>{{ u.id }}</td>
                  <td>{{ u.nombre }} {{ u.apellido }}</td>
                  <td>{{ u.email }}</td>
                  <td>
                    <select class="usuarios__select" [value]="u.rol" (change)="cambiarRol(u, $any($event.target).value)">
                      <option value="ADMIN">ADMIN</option>
                      <option value="CLIENTE">CLIENTE</option>
                    </select>
                  </td>
                  <td>
                    <button
                      [class]="'boton boton-sm ' + (u.activo ? 'boton-exito' : 'boton-peligro')"
                      (click)="cambiarActivo(u)">
                      {{ u.activo ? 'Activo' : 'Inactivo' }}
                    </button>
                  </td>
                  <td>
                    <button class="boton boton-ghost boton-sm" (click)="editar(u)">Editar</button>
                    <button class="boton boton-peligro boton-sm" (click)="eliminar(u)">Eliminar</button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="vacio">No hay usuarios.</td></tr>
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
        <h3>{{ form.controls['id'].value ? 'Editar usuario' : 'Nuevo usuario' }}</h3>
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-grupo">
            <label>Nombres</label>
            <input formControlName="nombre" />
            @if (form.controls['nombre'].invalid && form.controls['nombre'].touched) {
              <div class="form-error">Obligatorio.</div>
            }
          </div>
          <div class="form-grupo">
            <label>Apellidos</label>
            <input formControlName="apellido" />
            @if (form.controls['apellido'].invalid && form.controls['apellido'].touched) {
              <div class="form-error">Obligatorio.</div>
            }
          </div>
          <div class="form-grupo">
            <label>Email</label>
            <input type="email" formControlName="email" />
            @if (form.controls['email'].invalid && form.controls['email'].touched) {
              <div class="form-error">Email válido requerido.</div>
            }
          </div>
          @if (!form.controls['id'].value) {
            <div class="form-grupo">
              <label>Contraseña</label>
              <input type="password" formControlName="password" placeholder="Mínimo 8 caracteres" />
            </div>
            <div class="form-grupo">
              <label>Rol</label>
              <select formControlName="rol">
                <option value="CLIENTE">CLIENTE</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          }
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
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit {
  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);

  readonly usuarios: WritableSignal<UsuarioResponse[]> = signal([]);
  readonly pagina: WritableSignal<PageResponse<UsuarioResponse> | null> = signal(null);
  readonly actual = signal(0);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly exito = signal('');

  form = this.fb.nonNullable.group({
    id: [null as number | null],
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    rol: ['CLIENTE' as Rol]
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.adminService.listarUsuarios({ pagina: this.actual(), tamanio: 10 }).subscribe({
      next: (res) => {
        this.usuarios.set(res.contenido);
        this.pagina.set(res);
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'Error al cargar usuarios.')
    });
  }

  cambiarPagina(p: number): void {
    this.actual.set(p);
    this.cargar();
  }

  nuevo(): void {
    this.form.reset({ id: null, nombre: '', apellido: '', email: '', password: '', rol: 'CLIENTE' });
  }

  editar(u: UsuarioResponse): void {
    this.form.setValue({
      id: u.id,
      nombre: u.nombre,
      apellido: u.apellido,
      email: u.email,
      password: '',
      rol: u.rol
    });
  }

  guardar(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set('');
    this.exito.set('');

    const operacion = v.id
      ? (() => {
          const request: UsuarioUpdateRequest = {
            nombre: v.nombre,
            apellido: v.apellido,
            email: v.email
          };
          return this.adminService.actualizarUsuario(v.id, request);
        })()
      : (() => {
          const request: UsuarioRequest = {
            nombre: v.nombre,
            apellido: v.apellido,
            email: v.email,
            password: v.password,
            rol: v.rol
          };
          return this.adminService.crearUsuario(request);
        })();

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.exito.set('Usuario guardado correctamente.');
        this.nuevo();
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el usuario.');
      }
    });
  }

  cambiarRol(u: UsuarioResponse, rol: Rol): void {
    if (u.rol === rol) return;
    this.adminService.cambiarRol(u.id, rol).subscribe({
      next: (actualizado) => this.usuarios.set(this.usuarios().map((x) => (x.id === actualizado.id ? actualizado : x))),
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'No se pudo cambiar el rol.');
        this.cargar();
      }
    });
  }

  cambiarActivo(u: UsuarioResponse): void {
    this.adminService.cambiarActivo(u.id, !u.activo).subscribe({
      next: (actualizado) => this.usuarios.set(this.usuarios().map((x) => (x.id === actualizado.id ? actualizado : x))),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo cambiar el estado.')
    });
  }

  eliminar(u: UsuarioResponse): void {
    if (!confirm(`¿Eliminar al usuario ${u.nombre} ${u.apellido}?`)) return;
    this.adminService.eliminarUsuario(u.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar.')
    });
  }
}