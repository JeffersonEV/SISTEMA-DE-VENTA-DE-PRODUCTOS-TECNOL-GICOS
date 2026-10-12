# INFORME — SEMANA 10 (contenido de apoyo para el Modelo de Informe)

Proyecto: **TechStore — Sistema de Venta de Productos Tecnológicos**
Curso: Desarrollo Web Integrado · Tunel: Ingeniería de Sistemas e Informática · Lima Centro

Este documento contiene el contenido técnico (secciones 4–9) listo para copiar/adaptar
en el **Modelo de Informe Semana 10**, citando el código real del repositorio, más la
**lista de capturas** que debes tomar tú mismo.

---

## 4. Arquitectura y estructura de paquetes

**Backend** (arquitectura por capas, paquete base `com.sistematecnologico`):

```
controller/   servicio/     repository/      entity/       dto/      security/      config/      exception/
```

- **controller/** — `@RestController` (Auth, Productos, Categorías, Pedidos, Admin Pedidos/Usuarios/Reportes, Usuarios). No contienen lógica de negocio.
- **service/** — reglas de negocio, `@Transactional`, **inyección por constructor** (sin `@Autowired` en campos).
- **repository/** — interfaces Spring Data JPA con derived queries y JPQL `@Query`.
- **entity/** — entidades JPA + enums `Rol`, `EstadoPedido`.
- **dto/** — records de request/response. **Nunca se exponen entidades JPA en JSON ni passwords.**
- **security/** — `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, entry point 401 y handler 403.
- **config/** — `SecurityConfig` (SecurityFilterChain, CORS), `OpenApiConfig`, `DataInitializer`.
- **exception/** — `GlobalExceptionHandler` (`@RestControllerAdvice`) que devuelve un JSON uniforme.

**Frontend** (`frontend/`) — Angular: `core/` (services, interceptors, guards, models), `shared/`, `features/{auth,catalogo,carrito,pedidos,admin}`.

## 5. Tabla de endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/registro` | Público | Registro → `{token, tipo:"Bearer", id, nombre, email, rol}` |
| POST | `/api/auth/login` | Público | Login → token JWT |
| GET | `/api/categorias` | Público | Lista categorías |
| GET | `/api/categorias/{id}` | Público | Categoría por id |
| POST | `/api/categorias` | ADMIN | Crea categoría (201) |
| PUT | `/api/categorias/{id}` | ADMIN | Actualiza (200) |
| DELETE | `/api/categorias/{id}` | ADMIN | Elimina (204) o 400 si tiene productos |
| GET | `/api/productos` | Público | Catálogo paginado + filtros `nombre`, `categoriaId`, `minPrecio`, `maxPrecio`, `soloActivos` |
| GET | `/api/productos/{id}` | Público | Detalle (200/404) |
| POST | `/api/productos` | ADMIN | Crea (201) |
| PUT | `/api/productos/{id}` | ADMIN | Actualiza (200) |
| DELETE | `/api/productos/{id}` | ADMIN | Elimina (204); borrado lógico si tiene historial |
| POST | `/api/pedidos` | CLIENTE/ADMIN | Crea pedido `{items:[{productoId,cantidad}]}` (201); valida stock, descuenta inventario, calcula total |
| GET | `/api/pedidos/mis-pedidos` | CLIENTE/ADMIN | Historial propio |
| GET | `/api/pedidos/{id}` | propio/ADMIN | Detalle (200/403/404) |
| PATCH | `/api/pedidos/{id}/cancelar` | propio/ADMIN | Cancela si PENDIENTE (200); restaura stock |
| GET | `/api/admin/pedidos` | ADMIN | Todos con filtros estado/usuario/fechas (paginado) |
| PATCH | `/api/admin/pedidos/{id}/estado` | ADMIN | Cambia estado (PENDIENTE/PAGADO/CANCELADO) |
| GET | `/api/admin/usuarios` | ADMIN | Usuarios paginados |
| GET | `/api/admin/usuarios/{id}` | ADMIN | Usuario |
| POST | `/api/admin/usuarios` | ADMIN | Crea usuario con rol |
| PUT | `/api/admin/usuarios/{id}` | ADMIN | Actualiza datos |
| PATCH | `/api/admin/usuarios/{id}/activo` | ADMIN | Activa/desactiva |
| PATCH | `/api/admin/usuarios/{id}/rol` | ADMIN | Cambia rol |
| DELETE | `/api/admin/usuarios/{id}` | ADMIN | Elimina (204) |
| GET | `/api/usuarios` · `/api/usuarios/{id}` · POST · PUT · DELETE | ADMIN | CRUD de usuarios (modelo de informe) |
| GET | `/api/admin/reportes/resumen` | ADMIN | KPIs: total ventas, pedidos por estado, usuarios, productos |
| GET | `/api/admin/reportes/top-productos?limite=` | ADMIN | Más vendidos |
| GET | `/api/admin/reportes/ventas-por-categoria` | ADMIN | Ventas agrupadas |
| GET | `/api/admin/reportes/stock-bajo?umbral=` | ADMIN | Reposición |
| GET | `/swagger-ui.html` | Público | Swagger UI |
| GET | `/actuator/health` | Público | Health check |

Códigos de estado utilizados: `200 OK` · `201 Created` · `204 No Content` · `400` validación/regla · `401` no autenticado · `403` sin rol · `404` no encontrado · `409` conflicto (email/nombre duplicado).

## 6. DTOs (paquete `dto/`)

- `auth/`: `RegistroRequest`, `LoginRequest`, `AuthResponse`.
- `usuario/`: `UsuarioRequest` (alta con rol), `UsuarioUpdateRequest`, `RolUpdateRequest`, `UsuarioResponse` (sin password).
- `categoria/`: `CategoriaRequest`, `CategoriaResponse`.
- `producto/`: `ProductoRequest`, `ProductoResponse`.
- `pedido/`: `PedidoRequest`, `PedidoItemRequest`, `EstadoPedidoRequest`, `PedidoResponse`, `DetallePedidoResponse`.
- `reporte/`: `ResumenReporte`, `TopProducto`, `VentasPorCategoria`, `ProductoStockBajo` (también proyecciones JPQL).
- `common/`: `PageResponse<T>`, `ErrorResponse`.

Se usan `record` de Java: inmutables y sin boilerplate; las respuestas nunca contienen `password` ni colecciones Lazy de entidades.

## 7. Entidades y relaciones (anotaciones clave)

| Entidad | Anotaciones principales |
|---|---|
| `Usuario` | `@Entity @Table(name="usuarios", uniqueConstraints=...)`, `@Id @GeneratedValue(IDENTITY)`, `@Enumerated(EnumType.STRING) rol`, `@OneToMany(mappedBy="usuario", cascade=ALL, orphanRemoval=true) pedidos`, `@PrePersist` |
| `Categoria` | `@Table(name="categorias")`, nombre `unique`, `@OneToMany(mappedBy="categoria", LAZY) productos` |
| `Producto` | `@ManyToOne(fetch=LAZY, optional=false) @JoinColumn(name="categoria_id")`, precio `BigDecimal`, `activo` (borrado lógico) |
| `Pedido` | `@ManyToOne usuario`, `@Column fecha`, `@Enumerated(STRING) estado`, `total`, `@OneToMany(mappedBy="pedido", cascade=ALL, orphanRemoval=true) @Builder.Default detalles`, helper `addDetalle()` |
| `DetallePedido` | `@ManyToOne pedido`, `@ManyToOne producto`, `cantidad`, `precioUnitario`, `subtotal` |

Todo `LAZY`; la serialización se hace siempre a DTO dentro de la transacción del service.

## 8. Consultas JPQL reales del código

```java
// ProductoRepository — filtros opcionales (paginado)
@Query("""
    SELECT p FROM Producto p
    WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
      AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
      AND (:minPrecio IS NULL OR p.precio >= :minPrecio)
      AND (:maxPrecio IS NULL OR p.precio <= :maxPrecio)
      AND (:activo IS NULL OR p.activo = :activo)
    """)
Page<Producto> buscarConFiltros(...);

// ProductoRepository — stock bajo
@Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= :umbral ORDER BY p.stock ASC")
List<Producto> productosStockBajo(@Param("umbral") int umbral);

// PedidoRepository — historial por usuario
@Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.fecha DESC")
List<Pedido> historialPorUsuario(@Param("usuarioId") Long usuarioId);

// PedidoRepository — estado y rango de fechas
@Query("SELECT p FROM Pedido p WHERE p.estado = :estado AND p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha DESC")
List<Pedido> porEstadoYRangoFechas(...);

// PedidoRepository — total de ventas
@Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO")
BigDecimal totalVentas();

// DetallePedidoRepository — top productos (JOIN + GROUP BY + SUM) con proyección a DTO
@Query("""
    SELECT new com.sistematecnologico.dto.reporte.TopProducto(
               dp.producto.id, dp.producto.nombre, SUM(dp.cantidad), SUM(dp.subtotal))
    FROM DetallePedido dp
    WHERE dp.pedido.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO
    GROUP BY dp.producto.id, dp.producto.nombre
    ORDER BY SUM(dp.cantidad) DESC
    """)
List<TopProducto> topProductos(Pageable pageable);

// DetallePedidoRepository — ventas por categoría
@Query("""
    SELECT new com.sistematecnologico.dto.reporte.VentasPorCategoria(
               c.nombre, SUM(dp.cantidad), SUM(dp.subtotal))
    FROM DetallePedido dp JOIN dp.producto p JOIN p.categoria c
    WHERE dp.pedido.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO
    GROUP BY c.nombre ORDER BY SUM(dp.subtotal) DESC
    """)
List<VentasPorCategoria> ventasPorCategoria();
```

**Derived queries:** `findByEmail`, `existsByEmail`, `existsByEmailAndIdNot`, `existsByNombreIgnoreCase`, `existsByCategoriaId`, `countByEstado`, `findByUsuarioIdOrderByFechaDesc`.

## 9. Seguridad y endpoints protegidos por rol

- `SecurityFilterChain` **stateless**, CSRF deshabilitado (API), **CORS global** configurable.
- `BCryptPasswordEncoder`; `JwtService` (HS256, claims `sub`=email, `uid`, `rol`); `JwtAuthenticationFilter` valida el Bearer y puebla `SecurityContext`.
- Entry point → **401 JSON**; Access Denied → **403 JSON**; `GlobalExceptionHandler` → JSON uniforme.

Reglas (`SecurityConfig.securityFilterChain`):

| Patrón | Rol |
|---|---|
| `/api/auth/**`, Swagger, `/actuator/health` | Público |
| `GET /api/productos/**`, `GET /api/categorias/**` | Público |
| POST/PUT/PATCH/DELETE `/api/productos/**` y `/api/categorias/**` | `ROLE_ADMIN` |
| `/api/admin/**`, `/api/usuarios/**` | `ROLE_ADMIN` |
| `/api/pedidos/**` | `ROLE_CLIENTE` o `ROLE_ADMIN` |
| resto | Autenticado |

Verificación automatizada: `AuthIntegrationTest` comprueba 401 sin token, 403 con CLIENTE escribiendo productos, 201 con ADMIN, flujo completo de pedido.

---

## Lista de capturas que debes tomar (tú, con tus herramientas)

1. **Swagger UI** (`http://localhost:8080/swagger-ui.html`) con el botón **Authorize** ingresando el token; captura de un endpoint expandido (ej. `POST /api/productos`).
2. **Postman:** colección `docs/postman/TechStore.postman_collection.json` importada; captura de: login (respuesta con token), crear producto (201), crear pedido (201), cancelar pedido (200), `GET /api/admin/reportes/resumen`.
3. **Códigos de estado:** capturas de 401 (sin token), 403 (CLIENTE intentando crear producto), 400 (payload inválido) y 409 (email duplicado).
4. **pgAdmin/Workbench:** captura de las tablas `usuarios`, `categorias`, `productos`, `pedidos`, `detalles_pedido` (esquema y una fila de cada una, mostrando el hash BCrypt).
5. **Tests:** captura de la terminal con `./mvnw test` (13 tests OK).
6. **Consola H2 (opcional):** si usas el perfil `h2`, captura la BD en memoria.
7. **Git/GitHub:** captura del historial de commits de la rama principal (commits frecuentes).
8. **Frontend (Fase 2):** capturas de la SPA: catálogo, login, registro, carrito, mis pedidos, panel admin (productos/categorías/pedidos/usuarios/reportes).
9. **Despliegue (Fase 3):** captura del backend corriendo en Render/Railway (log de inicio + URL) y del frontend publicado en Vercel/Netlify.