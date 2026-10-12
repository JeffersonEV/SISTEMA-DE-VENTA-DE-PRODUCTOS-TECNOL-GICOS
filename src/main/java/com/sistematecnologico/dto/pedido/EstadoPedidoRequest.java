package com.sistematecnologico.dto.pedido;

import com.sistematecnologico.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoPedidoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) {
}
