package br.com.aygean.apirest.service;


import br.com.aygean.apirest.model.Produto;
import br.com.aygean.apirest.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {
    private static final String TOPICO_PEDIDOS = "pedidos";

    private final ProdutoRepository prepository;


    public ProdutoService(ProdutoRepository prepository) {
        this.prepository = prepository;
    }

    public Produto buscarProdutoPorId(Long id) {
        return prepository.findById(id).get();
    }
}
