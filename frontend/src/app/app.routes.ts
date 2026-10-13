import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo.component').then((m) => m.CatalogoComponent) },
  { path: 'catalogo/:id', loadComponent: () => import('./features/catalogo/detalle/detalle.component').then((m) => m.DetalleComponent) },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'registro',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/registro/registro.component').then((m) => m.RegistroComponent)
  },
  {
    path: 'carrito',
    loadComponent: () => import('./features/carrito/carrito.component').then((m) => m.CarritoComponent)
  },
  {
    path: 'mis-pedidos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/pedidos/mis-pedidos.component').then((m) => m.MisPedidosComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/admin-layout/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then((m) => m.DashboardComponent) },
      { path: 'categorias', loadComponent: () => import('./features/admin/categorias/categorias.component').then((m) => m.CategoriasComponent) },
      { path: 'productos', loadComponent: () => import('./features/admin/productos/productos.component').then((m) => m.ProductosComponent) },
      { path: 'pedidos', loadComponent: () => import('./features/admin/pedidos/pedidos.component').then((m) => m.PedidosComponent) },
      { path: 'usuarios', loadComponent: () => import('./features/admin/usuarios/usuarios.component').then((m) => m.UsuariosComponent) },
      { path: 'reportes', loadComponent: () => import('./features/admin/reportes/reportes.component').then((m) => m.ReportesComponent) }
    ]
  },
  { path: '**', loadComponent: () => import('./shared/components/not-found/not-found.component').then((m) => m.NotFoundComponent) }
];