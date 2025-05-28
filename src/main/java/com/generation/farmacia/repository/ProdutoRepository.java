package com.generation.farmacia.repository;

import com.generation.farmacia.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Importe Optional

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método de busca personalizado: encontra produtos pelo 'nome' ignorando maiúsculas/minúsculas
    List<Produto> findAllByNomeContainingIgnoreCase(String nome);

    // Opcional: Métodos para buscar produtos por preço (ex: preco maior que, menor que, entre)
    // List<Produto> findByPrecoLessThanEqual(BigDecimal preco);
    // List<Produto> findByPrecoGreaterThanEqual(BigDecimal preco);
    // List<Produto> findByPrecoBetween(BigDecimal precoInicial, BigDecimal precoFinal);
}