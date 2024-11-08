package br.com.aygean.apirest.controller;


import br.com.aygean.apirest.dto.ListaPedidoRequest;
import br.com.aygean.apirest.dto.PedidoRequest;
import br.com.aygean.apirest.model.Pedido;
import br.com.aygean.apirest.model.Produto;
import br.com.aygean.apirest.service.PedidoService;
import br.com.aygean.apirest.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {


    private final PedidoService pedidoService;
    private final ProdutoService produtoService;

    public PedidoController(PedidoService pedidoService, ProdutoService produtoService) {
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
    }

    // Endpoint para criar um novo pedido
    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody ListaPedidoRequest pedidoRequest) {
        if (pedidoRequest==null) {
            return ResponseEntity.badRequest().build();
        }
        var pedido = pedidoService.criarPedido(pedidoRequest);
        return ResponseEntity.ok(pedido);
    }

    // Endpoint para consultar um pedido específico
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> consultarPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.consultarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}