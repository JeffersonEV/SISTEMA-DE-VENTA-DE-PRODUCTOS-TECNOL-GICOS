import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EstadoPedido,
  PageResponse,
  PedidoResponse,
  ProductoStockBajo,
  ResumenReporte,
  TopProducto,
  UsuarioRequest,
  UsuarioResponse,
  UsuarioUpdateRequest,
  VentasPorCategoria
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}

  listarPedidos(params: {
    pagina?: number;
    tamanio?: number;
    estado?: EstadoPedido;
    usuarioId?: number;
    desde?: string;
    hasta?: string;
  }): Observable<PageResponse<PedidoResponse>> {
    let p = new HttpParams().set('page', params.pagina ?? 0).set('size', params.tamanio ?? 10);
    if (params.estado) p = p.set('estado', params.estado);
    if (params.usuarioId) p = p.set('usuarioId', String(params.usuarioId));
    if (params.desde) p = p.set('desde', this.aFechaHora(params.desde, false));
    if (params.hasta) p = p.set('hasta', this.aFechaHora(params.hasta, true));
    return this.http.get<PageResponse<PedidoResponse>>(`${environment.apiUrl}/admin/pedidos`, { params: p });
  }

  private aFechaHora(fecha: string, finDeDia: boolean): string {
    if (fecha.includes('T')) return fecha;
    return `${fecha}T${finDeDia ? '23:59:59' : '00:00:00'}`;
  }

  cambiarEstado(id: number, estado: EstadoPedido): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(`${environment.apiUrl}/admin/pedidos/${id}/estado`, { estado });
  }

  listarUsuarios(params: { pagina?: number; tamanio?: number }): Observable<PageResponse<UsuarioResponse>> {
    const p = new HttpParams().set('page', params.pagina ?? 0).set('size', params.tamanio ?? 10);
    return this.http.get<PageResponse<UsuarioResponse>>(`${environment.apiUrl}/admin/usuarios`, { params: p });
  }

  crearUsuario(usuario: UsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${environment.apiUrl}/admin/usuarios`, usuario);
  }

  actualizarUsuario(id: number, usuario: UsuarioUpdateRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${environment.apiUrl}/admin/usuarios/${id}`, usuario);
  }

  cambiarActivo(id: number, activo: boolean): Observable<UsuarioResponse> {
    return this.http.patch<UsuarioResponse>(`${environment.apiUrl}/admin/usuarios/${id}/activo`, { activo });
  }

  cambiarRol(id: number, rol: 'ADMIN' | 'CLIENTE'): Observable<UsuarioResponse> {
    return this.http.patch<UsuarioResponse>(`${environment.apiUrl}/admin/usuarios/${id}/rol`, { rol });
  }

  eliminarUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/admin/usuarios/${id}`);
  }

  resumen(): Observable<ResumenReporte> {
    return this.http.get<ResumenReporte>(`${environment.apiUrl}/admin/reportes/resumen`);
  }

  topProductos(limite = 5): Observable<TopProducto[]> {
    return this.http.get<TopProducto[]>(`${environment.apiUrl}/admin/reportes/top-productos?limite=${limite}`);
  }

  ventasPorCategoria(): Observable<VentasPorCategoria[]> {
    return this.http.get<VentasPorCategoria[]>(`${environment.apiUrl}/admin/reportes/ventas-por-categoria`);
  }

  stockBajo(umbral = 5): Observable<ProductoStockBajo[]> {
    return this.http.get<ProductoStockBajo[]>(`${environment.apiUrl}/admin/reportes/stock-bajo?umbral=${umbral}`);
  }
}