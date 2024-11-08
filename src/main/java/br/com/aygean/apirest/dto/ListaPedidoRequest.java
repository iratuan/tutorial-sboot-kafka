package br.com.aygean.apirest.dto;

import java.util.List;

public record ListaPedidoRequest(Long clienteId, List<PedidoRequest> pedidos) {
}
