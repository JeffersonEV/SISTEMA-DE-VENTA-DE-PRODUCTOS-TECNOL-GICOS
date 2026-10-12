import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CategoriaResponse } from '../models/models';

export interface CategoriaRequest {
  nombre: string;
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  constructor(private http: HttpClient) {}

  listar(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(`${environment.apiUrl}/categorias`);
  }

  obtener(id: number): Observable<CategoriaResponse> {
    return this.http.get<CategoriaResponse>(`${environment.apiUrl}/categorias/${id}`);
  }

  crear(categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(`${environment.apiUrl}/categorias`, categoria);
  }

  actualizar(id: number, categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(`${environment.apiUrl}/categorias/${id}`, categoria);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/categorias/${id}`);
  }
}