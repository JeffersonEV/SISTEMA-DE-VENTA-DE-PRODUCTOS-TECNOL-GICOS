import { Injectable, computed, signal } from '@angular/core';
import { ProductoResponse } from '../models/models';
import { AuthService } from './auth.service';

export interface ItemCarrito {
  producto: ProductoResponse;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly KEY = 'techstore_carrito';
  private readonly itemsSignal = signal<ItemCarrito[]>(this.cargar());

  readonly items = this.itemsSignal.asReadonly();
  readonly cantidadTotal = computed(() =>
    this.items().reduce((acc, i) => acc + i.cantidad, 0)
  );
  readonly total = computed(() =>
    this.items().reduce((acc, i) => acc + i.cantidad * i.producto.precio, 0)
  );

  constructor(private auth: AuthService) {}

  agregar(producto: ProductoResponse, cantidad = 1): void {
    const actual = this.items();
    const existente = actual.find((i) => i.producto.id === producto.id);
    const nuevos = existente
      ? actual.map((i) =>
          i.producto.id === producto.id
            ? { ...i, cantidad: Math.min(i.cantidad + cantidad, producto.stock) }
            : i
        )
      : [...actual, { producto, cantidad: Math.min(cantidad, producto.stock) }];
    this.itemsSignal.set(nuevos);
    this.guardar();
  }

  cambiarCantidad(id: number, cantidad: number): void {
    const nuevos = this.items().map((i) =>
      i.producto.id === id
        ? { ...i, cantidad: Math.max(1, Math.min(cantidad, i.producto.stock)) }
        : i
    );
    this.itemsSignal.set(nuevos);
    this.guardar();
  }

  quitar(id: number): void {
    this.itemsSignal.set(this.items().filter((i) => i.producto.id !== id));
    this.guardar();
  }

  vaciar(): void {
    this.itemsSignal.set([]);
    this.guardar();
  }

  private guardar(): void {
    localStorage.setItem(this.KEY, JSON.stringify(this.items()));
  }

  private cargar(): ItemCarrito[] {
    try {
      const raw = localStorage.getItem(this.KEY);
      return raw ? (JSON.parse(raw) as ItemCarrito[]) : [];
    } catch {
      return [];
    }
  }
}