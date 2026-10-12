package com.sistematecnologico.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PedidoRequest(
        @NotEmpty(message = "El pedido debe contener al menos un producto")
        List<@Valid PedidoItemRequest> items
) {
}
