import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PedidoResponse } from '../models/models';

export interface PedidoItemRequest {
  productoId: number;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class PedidoService {
  constructor(private http: HttpClient) {}

  crear(items: PedidoItemRequest[]): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${environment.apiUrl}/pedidos`, { items });
  }

  misPedidos(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${environment.apiUrl}/pedidos/mis-pedidos`);
  }

  obtener(id: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${environment.apiUrl}/pedidos/${id}`);
  }

  cancelar(id: number): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(`${environment.apiUrl}/pedidos/${id}/cancelar`, {});
  }
}