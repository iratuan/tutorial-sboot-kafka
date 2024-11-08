package br.com.aygean.apirest.service;


import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.dto.ListaPedidoRequest;
import br.com.aygean.apirest.exception.ClienteNotFoundException;
import br.com.aygean.apirest.exception.PedidoNotFoundException;
import br.com.aygean.apirest.model.Pedido;
import br.com.aygean.apirest.model.Produto;
import br.com.aygean.apirest.repository.ClienteRepository;
import br.com.aygean.apirest.repository.PedidoRepository;
import br.com.aygean.apirest.repository.ProdutoRepository;
import br.com.aygean.apirest.service.producer.EmailProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final String TOPICO_PEDIDOS = "pedidos";


    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final EmailProducer emailProducer;

    @Autowired
    private KafkaTemplate<String, Pedido> kafkaTemplate;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, ClienteRepository clienteRepository, EmailProducer emailProducer) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.emailProducer = emailProducer;
    }

    @Transactional
    public Pedido criarPedido(ListaPedidoRequest pedidoRequest) {
        var cliente = clienteRepository.findById(pedidoRequest.clienteId()).orElseThrow(() -> new ClienteNotFoundException());
        Map<Produto, Integer> produtos = pedidoRequest.pedidos().stream()
                .collect(Collectors.toMap(
                        p -> produtoRepository.findById(p.codigoProduto()).get(), // chave: Produto
                        p -> p.quantidade()     // valor: quantidade
                ));
        var pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProdutos(produtos.keySet());
        pedido.setStatus("EM_PROCESSAMENTO");
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);
        kafkaTemplate.send(TOPICO_PEDIDOS, pedido);

        // Enviando para a fila de envio de email
        emailProducer.enviarParaFila(new EmailRequest(pedido.getCliente().getId(), pedido.getId()));

        return pedido;
    }

    @Transactional
    public Pedido atualizarStatusPedido(Long id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado"));
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public Pedido consultarPedido(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado"));
    }
}
