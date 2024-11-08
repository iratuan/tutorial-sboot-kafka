package br.com.aygean.apirest.dto;

public record PedidoRequest(Long codigoCliente, Long codigoProduto, Integer quantidade) {
}
