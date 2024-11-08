package br.com.aygean.apirest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_envio_email")
@Data
@NoArgsConstructor
public class RegistroEnvioEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private Long pedidoId;

    private LocalDateTime dataEnvio;
}
