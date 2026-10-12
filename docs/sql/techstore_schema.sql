-- ============================================================================
-- TechStore - Sistema de Venta de Productos Tecnologicos
-- DDL PostgreSQL generado a partir de las entidades JPA (Hibernate 7).
-- Ejecutar en la base `sistema_tecnologico`:
--   psql -U postgres -d sistema_tecnologico -f techstore_schema.sql
--
-- NOTA: El proyecto usa `spring.jpa.hibernate.ddl-auto=update` y crea el
-- esquema automaticamente. Este script es la evidencia documental del DDL y
-- los datos de ejemplo (plica para el informe de la Semana 10).
-- Las contrasenas se almacenan SOLO como hash BCrypt (nunca en texto plano).
-- ============================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    apellido        VARCHAR(80)  NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'CLIENTE')),
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS productos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio      NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    stock       INTEGER      NOT NULL DEFAULT 0 CHECK (stock >= 0),
    marca       VARCHAR(80),
    imagen_url  VARCHAR(500),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    categoria_id BIGINT      NOT NULL REFERENCES categorias(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS pedidos (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado     VARCHAR(20)   NOT NULL CHECK (estado IN ('PENDIENTE', 'PAGADO', 'CANCELADO')),
    total      NUMERIC(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS detalles_pedido (
    id             BIGSERIAL PRIMARY KEY,
    pedido_id      BIGINT        NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id    BIGINT        NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad       INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL,
    subtotal       NUMERIC(12,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_activo     ON productos(activo);
CREATE INDEX IF NOT EXISTS idx_pedidos_usuario      ON pedidos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado       ON pedidos(estado);
CREATE INDEX IF NOT EXISTS idx_detalles_producto    ON detalles_pedido(producto_id);

-- ============================================================================
-- DATOS DE EJEMPLO (credentiales demo)
--   admin  -> admin@techstore.com   / Admin123*
--   cliente-> cliente@techstore.com / Cliente123*
-- Hash BCrypt generados con BCryptPasswordEncoder (strength 10), originalmente
-- producidos por el DataInitializer de la aplicacion.
-- ============================================================================

INSERT INTO usuarios (nombre, apellido, email, password, activo, rol)
VALUES
('Admin', 'TechStore', 'admin@techstore.com', '$2a$10$0X2kZDDb2hBePvljt/AAPeNx0YlTBHj4Z/pbGypC9AWFqG7MJMRku', TRUE, 'ADMIN'),
('Cliente', 'Demo', 'cliente@techstore.com', '$2a$10$bueXvzmCeK2ZRio514I6m.jo.Va91gnd1ia5MS5VehkaAHn2dWO2G', TRUE, 'CLIENTE');

INSERT INTO categorias (nombre, descripcion) VALUES
('Laptops',      'Computadoras portatiles para trabajo y gaming'),
('Celulares',    'Smartphones de ultima generacion'),
('Perifericos',  'Teclados, mouses, monitores y accesorios'),
('Audio',        'Audifonos, parlantes y microfonos'),
('Componentes',  'Tarjetas, memorias y almacenamiento');

INSERT INTO productos (nombre, descripcion, precio, stock, marca, imagen_url, activo, categoria_id) VALUES
('Laptop Lenovo IdeaPad 3', 'Ryzen 5, 16GB RAM, 512GB SSD', 4599.90, 12, 'Lenovo', 'https://picsum.photos/seed/laptop1/400/300', TRUE, 1),
('MacBook Air M3', 'Apple M3, 8GB RAM, 256GB SSD', 6999.00, 6, 'Apple', 'https://picsum.photos/seed/macbook/400/300', TRUE, 1),
('Asus TUF Gaming F15', 'Intel i7, RTX 4060, 16GB RAM', 5899.00, 5, 'Asus', 'https://picsum.photos/seed/asus/400/300', TRUE, 1),
('Samsung Galaxy S24', '6.2", 256GB, 8GB RAM', 3299.90, 15, 'Samsung', 'https://picsum.photos/seed/s24/400/300', TRUE, 2),
('iPhone 15', '6.1", 128GB, A16 Bionic', 4199.00, 10, 'Apple', 'https://picsum.photos/seed/iphone15/400/300', TRUE, 2),
('Xiaomi Redmi Note 13', '6.67", 256GB, 8GB RAM', 1099.00, 25, 'Xiaomi', 'https://picsum.photos/seed/redmi/400/300', TRUE, 2),
('Teclado Mecanico Redragon', 'Switch red, retroiluminado RGB', 189.90, 40, 'Redragon', 'https://picsum.photos/seed/teclado/400/300', TRUE, 3),
('Mouse Logitech G502', 'Sensor HERO 25K, 11 botones', 249.90, 35, 'Logitech', 'https://picsum.photos/seed/mouse/400/300', TRUE, 3),
('Monitor LG UltraGear 27"', '27" QHD 165Hz IPS', 1399.00, 8, 'LG', 'https://picsum.photos/seed/monitor/400/300', TRUE, 3),
('Audifonos Sony WH-1000XM5', 'Cancelacion de ruido, 30h bateria', 1599.00, 14, 'Sony', 'https://picsum.photos/seed/sony/400/300', TRUE, 4),
('Parlante JBL Flip 6', 'Bluetooth, resistente al agua', 459.00, 20, 'JBL', 'https://picsum.photos/seed/jbl/400/300', TRUE, 4),
('SSD NVMe Kingston 1TB', 'PCIe Gen4, 7000MB/s', 389.90, 30, 'Kingston', 'https://picsum.photos/seed/ssd/400/300', TRUE, 5),
('Memoria RAM Corsair 16GB', 'DDR5 5200MHz', 329.00, 4, 'Corsair', 'https://picsum.photos/seed/ram/400/300', TRUE, 5);