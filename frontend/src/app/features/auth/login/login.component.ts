import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth">
      <form class="tarjeta auth__card" [formGroup]="form" (ngSubmit)="enviar()">
        <h2>Iniciar sesión</h2>
        <p class="auth__sub">Accede con tu cuenta para comprar y gestionar pedidos.</p>

        @if (error()) {
          <div class="mensaje-error">{{ error() }}</div>
        }

        <div class="form-grupo">
          <label for="email">Correo electrónico</label>
          <input id="email" type="email" formControlName="email" placeholder="tucorreo@ejemplo.com" autocomplete="email" />
          @if (form.controls['email'].invalid && form.controls['email'].touched) {
            <div class="form-error">Ingresa un correo válido.</div>
          }
        </div>

        <div class="form-grupo">
          <label for="password">Contraseña</label>
          <input id="password" type="password" formControlName="password" placeholder="••••••••" autocomplete="current-password" />
          @if (form.controls['password'].invalid && form.controls['password'].touched) {
            <div class="form-error">La contraseña es obligatoria.</div>
          }
        </div>

        <button class="boton boton-primario" type="submit" [disabled]="form.invalid || cargando()">
          {{ cargando() ? 'Ingresando…' : 'Ingresar' }}
        </button>

        <p class="auth__enlace">
          ¿No tienes cuenta? <a routerLink="/registro">Regístrate aquí</a>
        </p>
        <p class="auth__demo">
          Demo: <code>admin@techstore.com / Admin123*</code> · <code>cliente@techstore.com / Cliente123*</code>
        </p>
      </form>
    </div>
  `,
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  enviar(): void {
    if (this.form.invalid) return;
    this.cargando.set(true);
    this.error.set('');
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const destino = this.auth.esAdmin() ? '/admin' : '/';
        this.router.navigate([destino]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.mensaje ?? 'Credenciales incorrectas.');
      }
    });
  }
}