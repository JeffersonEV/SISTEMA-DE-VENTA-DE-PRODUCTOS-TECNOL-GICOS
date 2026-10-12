import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth">
      <form class="tarjeta auth__card" [formGroup]="form" (ngSubmit)="enviar()">
        <h2>Crear cuenta</h2>
        <p class="auth__sub">Regístrate y empieza a comprar en TechStore.</p>

        @if (error()) {
          <div class="mensaje-error">{{ error() }}</div>
        }

        <div class="form-grupo">
          <label for="nombre">Nombres</label>
          <input id="nombre" formControlName="nombre" placeholder="Juan Carlos" />
          @if (form.controls['nombre'].invalid && form.controls['nombre'].touched) {
            <div class="form-error">El nombre es obligatorio.</div>
          }
        </div>

        <div class="form-grupo">
          <label for="apellido">Apellidos</label>
          <input id="apellido" formControlName="apellido" placeholder="Pérez Gómez" />
          @if (form.controls['apellido'].invalid && form.controls['apellido'].touched) {
            <div class="form-error">El apellido es obligatorio.</div>
          }
        </div>

        <div class="form-grupo">
          <label for="email">Correo electrónico</label>
          <input id="email" type="email" formControlName="email" placeholder="tucorreo@ejemplo.com" />
          @if (form.controls['email'].invalid && form.controls['email'].touched) {
            <div class="form-error">Ingresa un correo válido.</div>
          }
        </div>

        <div class="form-grupo">
          <label for="password">Contraseña</label>
          <input id="password" type="password" formControlName="password" placeholder="Mínimo 8 caracteres" />
          @if (form.controls['password'].invalid && form.controls['password'].touched) {
            <div class="form-error">La contraseña debe tener al menos 8 caracteres.</div>
          }
        </div>

        <div class="form-grupo">
          <label for="confirmar">Confirmar contraseña</label>
          <input id="confirmar" type="password" formControlName="confirmar" placeholder="Repite la contraseña" />
          @if (form.invalid && form.controls['confirmar'].touched && form.hasError('coinciden')) {
            <div class="form-error">Las contraseñas no coinciden.</div>
          }
        </div>

        <button class="boton boton-primario" type="submit" [disabled]="form.invalid || cargando()">
          {{ cargando() ? 'Registrando…' : 'Registrarse' }}
        </button>

        <p class="auth__enlace">
          ¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a>
        </p>
      </form>
    </div>
  `,
  styleUrl: '../login/login.component.scss'
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group(
    {
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmar: ['', Validators.required]
    },
    { validators: (g) => (g.get('password')?.value === g.get('confirmar')?.value ? null : { coinciden: true }) }
  );

  enviar(): void {
    if (this.form.invalid) return;
    this.cargando.set(true);
    this.error.set('');
    const { confirmar: _confirmar, ...datos } = this.form.getRawValue();
    this.auth.registro(datos).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo registrar. Inténtalo de nuevo.');
      }
    });
  }
}