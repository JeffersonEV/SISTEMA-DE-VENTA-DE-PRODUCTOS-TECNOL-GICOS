package com.sistematecnologico.service;

import com.sistematecnologico.dto.pedido.PedidoItemRequest;
import com.sistematecnologico.dto.pedido.PedidoRequest;
import com.sistematecnologico.dto.pedido.PedidoResponse;
import com.sistematecnologico.entity.*;
import com.sistematecnologico.exception.ReglaNegocioException;
import com.sistematecnologico.repository.PedidoRepository;
import com.sistematecnologico.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario cliente;
    private Producto producto;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder()
                .id(1L).nombre("Cliente").apellido("Test")
                .email("cliente@test.com").rol(Rol.CLIENTE).activo(true).build();
        producto = Producto.builder()
                .id(10L).nombre("Laptop").precio(new BigDecimal("100.00"))
                .stock(10).activo(true).build();
    }

    @Test
    @DisplayName("crear(): descuenta stock y calcula el total en el backend")
    void crearDescuentaStockYCalculaTotal() {
        when(usuarioService.porEmail("cliente@test.com")).thenReturn(cliente);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoRequest request = new PedidoRequest(List.of(new PedidoItemRequest(10L, 3)));
        PedidoResponse response = pedidoService.crear("cliente@test.com", request);

        assertEquals(new BigDecimal("300.00"), response.total());
        assertEquals(7, producto.getStock());
        verify(productoRepository).save(producto);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUsuario());
        assertEquals(1, captor.getValue().getDetalles().size());
        assertEquals(EstadoPedido.PENDIENTE, response.estado());
        assertEquals(1, response.detalles().size());
    }

    @Test
    @DisplayName("crear(): lanza excepcion si el stock es insuficiente")
    void crearConStockInsuficienteLanzaExcepcion() {
        producto.setStock(2);
        when(usuarioService.porEmail("cliente@test.com")).thenReturn(cliente);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        PedidoRequest request = new PedidoRequest(List.of(new PedidoItemRequest(10L, 5)));

        assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crear("cliente@test.com", request));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear(): rechaza producto inactivo")
    void crearConProductoInactivoLanzaExcepcion() {
        producto.setActivo(false);
        when(usuarioService.porEmail("cliente@test.com")).thenReturn(cliente);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        PedidoRequest request = new PedidoRequest(List.of(new PedidoItemRequest(10L, 1)));

        assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crear("cliente@test.com", request));
    }

    @Test
    @DisplayName("crear(): agrega cantidades si el mismo producto se repite")
    void crearAgrupaCantidadesRepetidas() {
        when(usuarioService.porEmail("cliente@test.com")).thenReturn(cliente);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoRequest request = new PedidoRequest(List.of(
                new PedidoItemRequest(10L, 2),
                new PedidoItemRequest(10L, 3)));

        PedidoResponse response = pedidoService.crear("cliente@test.com", request);

        assertEquals(new BigDecimal("500.00"), response.total());
        assertEquals(5, producto.getStock());
    }

    @Test
    @DisplayName("cancelar(): devuelve el stock al inventario")
    void cancelarRestauraStock() {
        Pedido pedido = pedidoNuevo();
        when(pedidoRepository.findById(200L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.cancelar(200L, "cliente@test.com", false);

        assertEquals(EstadoPedido.CANCELADO, response.estado());
        assertEquals(13, producto.getStock(), "stock inicial 10 + cantidad devuelta 3");
    }

    @Test
    @DisplayName("cancelar(): solo permite cancelar pedidos PENDIENTE")
    void cancelarPedidoPagoLanzaExcepcion() {
        Pedido pedido = pedidoNuevo();
        pedido.setEstado(EstadoPedido.PAGADO);
        when(pedidoRepository.findById(200L)).thenReturn(Optional.of(pedido));

        assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cancelar(200L, "cliente@test.com", false));
    }

    @Test
    @DisplayName("cancelar(): un cliente no puede cancelar pedidos ajenos")
    void cancelarPedidoAjenoLanzaExcepcion() {
        Pedido pedido = pedidoNuevo();
        pedido.getUsuario().setEmail("otro@test.com");
        when(pedidoRepository.findById(200L)).thenReturn(Optional.of(pedido));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> pedidoService.cancelar(200L, "cliente@test.com", false));
    }

    private Pedido pedidoNuevo() {
        producto.setStock(10);
        Pedido pedido = Pedido.builder()
                .id(200L)
                .usuario(cliente)
                .fecha(java.time.LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .total(new BigDecimal("300.00"))
                .build();
        DetallePedido detalle = DetallePedido.builder()
                .producto(producto)
                .cantidad(3)
                .precioUnitario(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("300.00"))
                .build();
        pedido.addDetalle(detalle);
        return pedido;
    }
}