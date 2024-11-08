package br.com.aygean.apirest.service.producer;


import br.com.aygean.apirest.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {

    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);
    private static final String TOPIC = "envio_email";

    private final KafkaTemplate<String, EmailRequest> kafkaTemplate;

    public EmailProducer(KafkaTemplate<String, EmailRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarParaFila(EmailRequest emailRequest) {
        logger.info("Enviando mensagem para a fila {}: {}", TOPIC, emailRequest);
        kafkaTemplate.send(TOPIC, emailRequest);
    }
}
