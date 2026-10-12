export type Rol = 'ADMIN' | 'CLIENTE';
export type EstadoPedido = 'PENDIENTE' | 'PAGADO' | 'CANCELADO';

export interface AuthResponse {
  token: string;
  tipo: string;
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
}

export interface RegistroRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: Rol;
  activo: boolean;
  fechaRegistro: string;
}

export interface UsuarioRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface UsuarioUpdateRequest {
  nombre: string;
  apellido: string;
  email: string;
  password?: string;
}

export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface ProductoResponse {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoriaId: number;
  categoriaNombre: string;
  activo: boolean;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoriaId: number;
  activo: boolean;
}

export interface DetallePedidoResponse {
  id: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface PedidoResponse {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  fecha: string;
  estado: EstadoPedido;
  total: number;
  detalles: DetallePedidoResponse[];
}

export interface PageResponse<T> {
  content: T[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  ultima: boolean;
}

export interface ResumenReporte {
  totalVentas: number;
  totalPedidos: number;
  pedidosPendientes: number;
  pedidosPagados: number;
  pedidosCancelados: number;
  totalUsuarios: number;
  totalProductos: number;
}

export interface TopProducto {
  productoId: number;
  nombre: string;
  cantidadVendida: number;
  totalVendido: number;
}

export interface VentasPorCategoria {
  categoria: string;
  cantidadVendida: number;
  totalVendido: number;
}

export interface ProductoStockBajo {
  id: number;
  nombre: string;
  stock: number;
  precio: number;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  ruta: string;
}