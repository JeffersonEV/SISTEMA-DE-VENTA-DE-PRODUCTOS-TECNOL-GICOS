package com.sistematecnologico.dto.pedido;

import com.sistematecnologico.entity.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        String cliente,
        LocalDateTime fecha,
        EstadoPedido estado,
        BigDecimal total,
        List<DetallePedidoResponse> detalles
) {
}
