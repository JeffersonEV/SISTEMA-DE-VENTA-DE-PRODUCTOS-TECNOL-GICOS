import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ConfiguracionService } from '../../../core/services/configuracion.service';
import { ConfiguracionResponse } from '../../../core/models/models';

@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  template: `
    <footer class="pie">
      <div class="contenedor pie__grid">
        <div class="footer-col footer-marca">
          <a routerLink="/" class="pie__logo">Tech<span>Store</span></a>
          <p class="footer-texto">{{ config()?.descripcion || 'Tu tienda de tecnología: laptops, celulares, accesorios y más al mejor precio.' }}</p>
          <div class="redes">
            @if (config()?.facebookUrl) {
              <a [href]="config()!.facebookUrl" target="_blank" rel="noopener" class="red" title="Facebook">fb</a>
            }
            @if (config()?.instagramUrl) {
              <a [href]="config()!.instagramUrl" target="_blank" rel="noopener" class="red" title="Instagram">ig</a>
            }
            @if (config()?.twitterUrl) {
              <a [href]="config()!.twitterUrl" target="_blank" rel="noopener" class="red" title="Twitter / X">X</a>
            }
          </div>
        </div>

        <div class="footer-col">
          <h4>Tienda</h4>
          <a routerLink="/">Inicio</a>
          <a routerLink="/catalogo">Catálogo</a>
          <a routerLink="/carrito">Carrito</a>
        </div>

        <div class="footer-col">
          <h4>Cuenta</h4>
          <a routerLink="/login">Ingresar</a>
          <a routerLink="/registro">Registrarse</a>
          <a routerLink="/mis-pedidos">Mis pedidos</a>
        </div>

        <div class="footer-col">
          <h4>Contacto</h4>
          @if (config()?.emailContacto) { <a [href]="'mailto:' + config()!.emailContacto">{{ config()!.emailContacto }}</a> }
          @if (config()?.telefono) { <a [href]="'tel:' + config()!.telefono">{{ config()!.telefono }}</a> }
          @if (config()?.whatsapp) { <a [href]="'https://wa.me/' + soloDigitos(config()!.whatsapp)" target="_blank" rel="noopener">WhatsApp: {{ config()!.whatsapp }}</a> }
          @if (config()?.direccion) { <span>{{ config()!.direccion }}</span> }
          @if (!config()?.emailContacto && !config()?.telefono && !config()?.whatsapp && !config()?.direccion) {
            <span>TechStore Demo</span>
          }
        </div>
      </div>

      <div class="footer-banda">
        <div class="contenedor footer-banda__fila">
          <span>&copy; {{ anio }} {{ config()?.nombreTienda || 'TechStore' }}. Todos los derechos reservados.</span>
          <span>Proyecto académico de demostración</span>
        </div>
      </div>
    </footer>
  `,
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
  private service = inject(ConfiguracionService);
  readonly config = signal<ConfiguracionResponse | null>(null);
  readonly anio = new Date().getFullYear();

  constructor() {
    this.service.obtener().subscribe({
      next: (c) => this.config.set(c),
      error: () => this.config.set(null)
    });
  }

  soloDigitos(valor: string): string {
    return valor.replace(/\D/g, '');
  }
}
