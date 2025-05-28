package com.generation.farmacia.repository;

import com.generation.farmacia.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Importe Optional

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByTipoContainingIgnoreCase(String tipo);

    //Teste um Duplicatas: Buscar uma categoria pelo tipo, ignorando maiúsculas/minúsculas
    // Retorna Optional para lidar com a possibilidade de não encontrar
    Optional<Categoria> findByTipoIgnoreCase(String tipo);
}