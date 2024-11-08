package br.com.aygean.apirest.service.consumer;

import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.model.RegistroEnvioEmail;
import br.com.aygean.apirest.repository.RegistroEnvioEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);
    private final RegistroEnvioEmailRepository registroEnvioEmailRepository;

    public EmailConsumer(RegistroEnvioEmailRepository registroEnvioEmailRepository) {
        this.registroEnvioEmailRepository = registroEnvioEmailRepository;
    }

    @Transactional
    @KafkaListener(topics = "envio_email", groupId = "email_group")
    public void processarEnvioEmail(EmailRequest emailRequest) {
        logger.info("Processando envio de e-mail para Cliente ID: {}, Produto ID: {}", emailRequest.clienteId(), emailRequest.pedidoId());

        RegistroEnvioEmail registro = new RegistroEnvioEmail();
        registro.setClienteId(emailRequest.clienteId());
        registro.setPedidoId(emailRequest.pedidoId());
        registro.setDataEnvio(LocalDateTime.now());

        registroEnvioEmailRepository.save(registro);

        logger.info("Registro de envio de e-mail salvo: {}", registro);
    }
}
