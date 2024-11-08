package br.com.aygean.apirest.exception;

public class PedidoNotFoundException extends RuntimeException {

    // Construtor que aceita uma mensagem personalizada
    public PedidoNotFoundException(String message) {
        super(message);
    }

    // Construtor que aceita uma mensagem personalizada e uma causa
    public PedidoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Construtor padrão, sem mensagem
    public PedidoNotFoundException() {
        super("Pedido não encontrado.");
    }
}
