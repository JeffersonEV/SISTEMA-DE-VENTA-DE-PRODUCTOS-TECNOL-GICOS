import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegistroRequest } from '../models/models';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'techstore_token';
  private readonly USER_KEY = 'techstore_usuario';

  readonly user = signal<AuthResponse | null>(this.cargarUsuario());

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  get token(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  get tokenValido(): boolean {
    const token = this.token;
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  isLogueado(): boolean {
    return !!this.user() && this.tokenValido;
  }

  esAdmin(): boolean {
    return this.user()?.rol === 'ADMIN';
  }

  login(credenciales: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credenciales).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  registro(datos: RegistroRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/registro`, datos).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.user.set(null);
    this.router.navigate(['/login']);
  }

  private guardarSesion(res: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res));
    this.user.set(res);
  }

  private cargarUsuario(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      return null;
    }
  }
}