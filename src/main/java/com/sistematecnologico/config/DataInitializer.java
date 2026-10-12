package com.sistematecnologico.config;

import com.sistematecnologico.entity.Categoria;
import com.sistematecnologico.entity.Producto;
import com.sistematecnologico.entity.Rol;
import com.sistematecnologico.entity.Usuario;
import com.sistematecnologico.repository.CategoriaRepository;
import com.sistematecnologico.repository.ProductoRepository;
import com.sistematecnologico.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inicializa datos demo si la base de datos esta vacia.
 * Los usuarios antiguos con password en texto plano deben recrearse
 * (las contrasenas se almacenan con BCrypt).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarios();
        crearCatalogo();
    }

    private void crearUsuarios() {
        if (!usuarioRepository.existsByEmail("admin@techstore.com")) {
            usuarioRepository.save(Usuario.builder()
                    .nombre("Admin")
                    .apellido("TechStore")
                    .email("admin@techstore.com")
                    .password(passwordEncoder.encode("Admin123*"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build());
            log.info("Usuario ADMIN demo creado: admin@techstore.com");
        }

        if (!usuarioRepository.existsByEmail("cliente@techstore.com")) {
            usuarioRepository.save(Usuario.builder()
                    .nombre("Cliente")
                    .apellido("Demo")
                    .email("cliente@techstore.com")
                    .password(passwordEncoder.encode("Cliente123*"))
                    .rol(Rol.CLIENTE)
                    .activo(true)
                    .build());
            log.info("Usuario CLIENTE demo creado: cliente@techstore.com");
        }
    }

    private void crearCatalogo() {
        if (categoriaRepository.count() > 0 || productoRepository.count() > 0) {
            return;
        }

        Categoria laptops = categoriaRepository.save(Categoria.builder()
                .nombre("Laptops").descripcion("Computadoras portatiles para trabajo y gaming").build());
        Categoria celulares = categoriaRepository.save(Categoria.builder()
                .nombre("Celulares").descripcion("Smartphones de ultima generacion").build());
        Categoria perifericos = categoriaRepository.save(Categoria.builder()
                .nombre("Perifericos").descripcion("Teclados, mouses, monitores y accesorios").build());
        Categoria audio = categoriaRepository.save(Categoria.builder()
                .nombre("Audio").descripcion("Audifonos, parlantes y microfonos").build());
        Categoria componentes = categoriaRepository.save(Categoria.builder()
                .nombre("Componentes").descripcion("Tarjetas, memorias y almacenamiento").build());

        productoRepository.saveAll(List.of(
                producto("Laptop Lenovo IdeaPad 3", "Ryzen 5, 16GB RAM, 512GB SSD", "4599.90", 12, "Lenovo",
                        "https://picsum.photos/seed/laptop1/400/300", laptops),
                producto("MacBook Air M3", "Apple M3, 8GB RAM, 256GB SSD", "6999.00", 6, "Apple",
                        "https://picsum.photos/seed/macbook/400/300", laptops),
                producto("Asus TUF Gaming F15", "Intel i7, RTX 4060, 16GB RAM", "5899.00", 5, "Asus",
                        "https://picsum.photos/seed/asus/400/300", laptops),
                producto("Samsung Galaxy S24", "6.2\", 256GB, 8GB RAM", "3299.90", 15, "Samsung",
                        "https://picsum.photos/seed/s24/400/300", celulares),
                producto("iPhone 15", "6.1\", 128GB, A16 Bionic", "4199.00", 10, "Apple",
                        "https://picsum.photos/seed/iphone15/400/300", celulares),
                producto("Xiaomi Redmi Note 13", "6.67\", 256GB, 8GB RAM", "1099.00", 25, "Xiaomi",
                        "https://picsum.photos/seed/redmi/400/300", celulares),
                producto("Teclado Mecanico Redragon", "Switch red, retroiluminado RGB", "189.90", 40, "Redragon",
                        "https://picsum.photos/seed/teclado/400/300", perifericos),
                producto("Mouse Logitech G502", "Sensor HERO 25K, 11 botones", "249.90", 35, "Logitech",
                        "https://picsum.photos/seed/mouse/400/300", perifericos),
                producto("Monitor LG UltraGear 27\"", "27\" QHD 165Hz IPS", "1399.00", 8, "LG",
                        "https://picsum.photos/seed/monitor/400/300", perifericos),
                producto("Audifonos Sony WH-1000XM5", "Cancelacion de ruido, 30h bateria", "1599.00", 14, "Sony",
                        "https://picsum.photos/seed/sony/400/300", audio),
                producto("Parlante JBL Flip 6", "Bluetooth, resistente al agua", "459.00", 20, "JBL",
                        "https://picsum.photos/seed/jbl/400/300", audio),
                producto("SSD NVMe Kingston 1TB", "PCIe Gen4, 7000MB/s", "389.90", 30, "Kingston",
                        "https://picsum.photos/seed/ssd/400/300", componentes),
                producto("Memoria RAM Corsair 16GB", "DDR5 5200MHz", "329.00", 4, "Corsair",
                        "https://picsum.photos/seed/ram/400/300", componentes)
        ));

        log.info("Catalogo demo creado: {} categorias, {} productos",
                categoriaRepository.count(), productoRepository.count());
    }

    private Producto producto(String nombre, String descripcion, String precio, int stock,
                              String marca, String imagenUrl, Categoria categoria) {
        return Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(new BigDecimal(precio))
                .stock(stock)
                .marca(marca)
                .imagenUrl(imagenUrl)
                .activo(true)
                .categoria(categoria)
                .build();
    }
}
