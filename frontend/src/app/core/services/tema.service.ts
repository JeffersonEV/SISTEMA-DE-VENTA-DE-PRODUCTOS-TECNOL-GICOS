import { Injectable, signal } from '@angular/core';

export type Tema = 'claro' | 'oscuro';

const CLAVE_TEMA = 'techstore-tema';

@Injectable({ providedIn: 'root' })
export class TemaService {
  readonly tema = signal<Tema>(this.preferidoInicial());

  constructor() {
    this.aplicar(this.tema());
  }

  esOscuro(): boolean {
    return this.tema() === 'oscuro';
  }

  alternar(): void {
    this.tema.update((actual) => (actual === 'oscuro' ? 'claro' : 'oscuro'));
    this.aplicar(this.tema());
  }

  private aplicar(tema: Tema): void {
    document.documentElement.classList.toggle('tema-oscuro', tema === 'oscuro');
    try {
      localStorage.setItem(CLAVE_TEMA, tema);
    } catch {
      // localStorage no disponible (modo privado): se ignora.
    }
  }

  private preferidoInicial(): Tema {
    try {
      const guardado = localStorage.getItem(CLAVE_TEMA);
      if (guardado === 'claro' || guardado === 'oscuro') {
        return guardado;
      }
    } catch {
      // localStorage no disponible: se usa el tema por defecto.
    }
    return 'claro';
  }
}
