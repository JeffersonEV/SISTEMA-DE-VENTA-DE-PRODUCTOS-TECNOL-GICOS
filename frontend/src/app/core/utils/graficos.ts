export const COLORES_GRAFICA = ['#2563eb', '#7c3aed', '#0891b2', '#d97706', '#16a34a', '#db2777', '#0ea5e9', '#dc2626'];

export interface SegmentoGrafica {
  valor: number;
}

export function colorGrafica(indice: number): string {
  return COLORES_GRAFICA[indice % COLORES_GRAFICA.length];
}

export function fondoDonut(segmentos: SegmentoGrafica[]): string {
  const suma = segmentos.reduce((total, s) => total + (s.valor > 0 ? s.valor : 0), 0);
  if (suma <= 0) {
    return 'conic-gradient(#e2e8f0 0% 100%)';
  }
  let acumulado = 0;
  const partes = segmentos.map((s, i) => {
    const desde = (acumulado / suma) * 100;
    acumulado += s.valor > 0 ? s.valor : 0;
    const hasta = (acumulado / suma) * 100;
    return `${COLORES_GRAFICA[i % COLORES_GRAFICA.length]} ${desde}% ${hasta}%`;
  });
  return `conic-gradient(${partes.join(', ')})`;
}

export function porcentajeBarra(valor: number, maximo: number): number {
  if (maximo <= 0) {
    return 0;
  }
  return Math.max(4, Math.round((valor / maximo) * 100));
}
