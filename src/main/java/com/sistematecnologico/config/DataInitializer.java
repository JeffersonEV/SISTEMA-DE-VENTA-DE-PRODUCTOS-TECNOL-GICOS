package com.sistematecnologico.config;

import com.sistematecnologico.entity.Banner;
import com.sistematecnologico.entity.Categoria;
import com.sistematecnologico.entity.Configuracion;
import com.sistematecnologico.entity.Producto;
import com.sistematecnologico.entity.Rol;
import com.sistematecnologico.entity.Usuario;
import com.sistematecnologico.repository.BannerRepository;
import com.sistematecnologico.repository.CategoriaRepository;
import com.sistematecnologico.repository.ConfiguracionRepository;
import com.sistematecnologico.repository.ProductoRepository;
import com.sistematecnologico.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final BannerRepository bannerRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           BannerRepository bannerRepository,
                           ConfiguracionRepository configuracionRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.bannerRepository = bannerRepository;
        this.configuracionRepository = configuracionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarios();
        crearCatalogo();
        crearBanners();
        crearConfiguracion();
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
                producto("Laptop Lenovo IdeaPad 3", "Ryzen 5, 16GB RAM, 512GB SSD", "2899.90", 12, "Lenovo",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Lenovo_IdeaPad_110.jpg/960px-Lenovo_IdeaPad_110.jpg", laptops),
                producto("MacBook Air M3", "Apple M3, 8GB RAM, 256GB SSD", "5249.00", 6, "Apple",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/MacBook_Air_%28Early_2015%29.jpg/960px-MacBook_Air_%28Early_2015%29.jpg", laptops),
                producto("Asus TUF Gaming F15", "Intel i7, RTX 4060, 16GB RAM", "5799.00", 5, "Asus",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/5/57/ASUS_TUF_Gaming_5_Pro_Laptop.jpg/960px-ASUS_TUF_Gaming_5_Pro_Laptop.jpg", laptops),
                producto("Samsung Galaxy S24", "6.2\", 256GB, 8GB RAM", "3299.90", 15, "Samsung",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/46/Samsung_Galaxy_S24_%28webtekno%29_008.png/960px-Samsung_Galaxy_S24_%28webtekno%29_008.png", celulares),
                producto("iPhone 15", "6.1\", 128GB, A16 Bionic", "3599.00", 10, "Apple",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Back_of_iPhone_15.jpg/960px-Back_of_iPhone_15.jpg", celulares),
                producto("Xiaomi Redmi Note 13", "6.67\", 256GB, 8GB RAM", "999.00", 25, "Xiaomi",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Xiaomi_Redmi_Note_10_Pro.jpg/960px-Xiaomi_Redmi_Note_10_Pro.jpg", celulares),
                producto("Teclado Mecanico Redragon", "Switch red, retroiluminado RGB", "199.90", 40, "Redragon",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5a/Mechanical_Keyboard.jpg/960px-Mechanical_Keyboard.jpg", perifericos),
                producto("Mouse Logitech G502", "Sensor HERO 25K, 11 botones", "299.90", 35, "Logitech",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/5/58/Logitech_G500_20100311%2C_top_photo.jpg/960px-Logitech_G500_20100311%2C_top_photo.jpg", perifericos),
                producto("Monitor LG UltraGear 27\"", "27\" QHD 165Hz IPS", "1799.00", 8, "LG",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/LG_UltraWide_monitors.jpg/960px-LG_UltraWide_monitors.jpg", perifericos),
                producto("Audifonos Sony WH-1000XM5", "Cancelacion de ruido, 30h bateria", "1699.00", 14, "Sony",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/0/00/S%C5%82uchawki_referencyjne_K-701_firmy_AKG.jpg/330px-S%C5%82uchawki_referencyjne_K-701_firmy_AKG.jpg", audio),
                producto("Parlante JBL Flip 6", "Bluetooth, resistente al agua", "499.90", 20, "JBL",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/JBL_Flip_3_bluetooth_speaker_%28DSCF2653%29.jpg/330px-JBL_Flip_3_bluetooth_speaker_%28DSCF2653%29.jpg", audio),
                producto("SSD NVMe Kingston 1TB", "PCIe Gen4, 7000MB/s", "369.90", 30, "Kingston",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Samsung_870_QVO_8TB_SATA_2%2C5_Zoll_Internes_Solid_State_Drive_%28SSD%29_%28MZ-77Q8T0BW%29_20211008_SSD023_corr.png/330px-Samsung_870_QVO_8TB_SATA_2%2C5_Zoll_Internes_Solid_State_Drive_%28SSD%29_%28MZ-77Q8T0BW%29_20211008_SSD023_corr.png", componentes),
                producto("Memoria RAM Corsair 16GB", "DDR5 5200MHz", "309.90", 4, "Corsair",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Swissbit_2GB_PC2-5300U-555.jpg/330px-Swissbit_2GB_PC2-5300U-555.jpg", componentes)
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

    private void crearBanners() {
        if (bannerRepository.count() > 0) {
            return;
        }
        bannerRepository.saveAll(List.of(
                Banner.builder()
                        .titulo("Tecnología que impulsa tu mundo")
                        .subtitulo("Descubre laptops, celulares y accesorios de las mejores marcas.")
                        .textoBoton("Ver catálogo")
                        .enlaceUrl("/catalogo")
                        .orden(1)
                        .activo(true)
                        .build(),
                Banner.builder()
                        .titulo("Hasta 30% de descuento")
                        .subtitulo("Ofertas exclusivas en audio y periféricos por tiempo limitado.")
                        .textoBoton("Aprovechar ofertas")
                        .enlaceUrl("/catalogo")
                        .orden(2)
                        .activo(true)
                        .build(),
                Banner.builder()
                        .titulo("Potencia tu setup gamer")
                        .subtitulo("Monitores, teclados y mouse de alto rendimiento te esperan.")
                        .textoBoton("Ver productos")
                        .enlaceUrl("/catalogo")
                        .orden(3)
                        .activo(true)
                        .build()
        ));
        log.info("Banners demo creados: {}", bannerRepository.count());
    }

    private void crearConfiguracion() {
        if (configuracionRepository.existsById(1L)) {
            return;
        }
        configuracionRepository.save(Configuracion.builder()
                .id(1L)
                .nombreTienda("TechStore")
                .descripcion("Tienda especializada en tecnología: laptops, celulares, audio y componentes.")
                .build());
        log.info("Configuracion inicial creada");
    }
}
