import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse, ProductoRequest, ProductoResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  constructor(private http: HttpClient) {}

  listar(params: {
    pagina?: number;
    tamanio?: number;
    nombre?: string;
    categoriaId?: number;
    minPrecio?: number;
    maxPrecio?: number;
    soloActivos?: boolean;
  }): Observable<PageResponse<ProductoResponse>> {
    let httpParams = new HttpParams();
    httpParams = httpParams.set('page', params.pagina ?? 0);
    httpParams = httpParams.set('size', params.tamanio ?? 12);
    if (params.nombre) httpParams = httpParams.set('nombre', params.nombre);
    if (params.categoriaId) httpParams = httpParams.set('categoriaId', String(params.categoriaId));
    if (params.minPrecio) httpParams = httpParams.set('minPrecio', String(params.minPrecio));
    if (params.maxPrecio) httpParams = httpParams.set('maxPrecio', String(params.maxPrecio));
    if (params.soloActivos !== undefined) httpParams = httpParams.set('soloActivos', String(params.soloActivos));
    return this.http.get<PageResponse<ProductoResponse>>(`${environment.apiUrl}/productos`, { params: httpParams });
  }

  obtener(id: number): Observable<ProductoResponse> {
    return this.http.get<ProductoResponse>(`${environment.apiUrl}/productos/${id}`);
  }

  crear(producto: ProductoRequest): Observable<ProductoResponse> {
    return this.http.post<ProductoResponse>(`${environment.apiUrl}/productos`, producto);
  }

  actualizar(id: number, producto: ProductoRequest): Observable<ProductoResponse> {
    return this.http.put<ProductoResponse>(`${environment.apiUrl}/productos/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/productos/${id}`);
  }
}