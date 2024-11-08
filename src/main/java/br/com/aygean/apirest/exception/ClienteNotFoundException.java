package br.com.aygean.apirest.exception;

public class ClienteNotFoundException extends RuntimeException {

    // Construtor que aceita uma mensagem personalizada
    public ClienteNotFoundException(String message) {
        super(message);
    }

    // Construtor que aceita uma mensagem personalizada e uma causa
    public ClienteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Construtor padrão, sem mensagem
    public ClienteNotFoundException() {
        super("Cliente não encontrado.");
    }
}
