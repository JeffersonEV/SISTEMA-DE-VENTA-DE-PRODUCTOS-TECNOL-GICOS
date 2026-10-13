import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfiguracionResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  private http = inject(HttpClient);

  obtener(): Observable<ConfiguracionResponse> {
    return this.http.get<ConfiguracionResponse>(`${environment.apiUrl}/configuracion`);
  }
}
