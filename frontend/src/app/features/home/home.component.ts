import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  template: `
    <section class="hero">
      <div class="hero__badge">Bienvenido a TechStore</div>
      <h1>Tu tienda de tecnología<br />al mejor precio</h1>
      <p>Computadoras, laptops, componentes y accesorios con stock en tiempo real y entrega rápida.</p>
      <div class="hero__acciones">
        <a routerLink="/catalogo" class="boton boton-primario boton-lg">Ver catálogo</a>
        <a routerLink="/registro" class="boton boton-ghost boton-lg">Crear cuenta</a>
      </div>
    </section>
    <section class="cajas">
      <div class="caja">
        <h3>&#128218; Catálogo completo</h3>
        <p>Explora y filtra productos por categoría, precio y nombre.</p>
      </div>
      <div class="caja">
        <h3>&#128274; Compras seguras</h3>
        <p>Registra tu cuenta, consulta pedidos y cancela en línea.</p>
      </div>
      <div class="caja">
        <h3>&#128200; Panel administrativo</h3>
        <p>Gestión de productos, pedidos, usuarios y reportes de ventas.</p>
      </div>
    </section>
  `,
  styleUrl: './home.component.scss'
})
export class HomeComponent {}