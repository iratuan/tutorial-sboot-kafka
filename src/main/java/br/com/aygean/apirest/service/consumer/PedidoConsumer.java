package br.com.aygean.apirest.service.consumer;

import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.model.Pedido;
import br.com.aygean.apirest.repository.PedidoRepository;
import br.com.aygean.apirest.service.producer.EmailProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PedidoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PedidoConsumer.class);

    private final PedidoRepository pedidoRepository;

    public PedidoConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    @KafkaListener(topics = "pedidos", groupId = "purchase_order_group")
    public void processarPedido(Pedido pedido) {

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedido.getId());
        if (pedidoOptional.isPresent()) {
            logger.info("Recebido pedido para processamento: {}", pedido);
            Pedido pedidoExistente = pedidoOptional.get();
            logger.info("Pedido encontrado: {}. Atualizando status para AGUARDANDO_PAGAMENTO", pedidoExistente);

            // Atualiza o status para "AGUARDANDO_PAGAMENTO"
            pedidoExistente.setStatus("AGUARDANDO_PAGAMENTO");
            pedidoRepository.save(pedidoExistente);


            logger.info("Pedido atualizado e salvo: {}", pedidoExistente);
        }
    }
}
