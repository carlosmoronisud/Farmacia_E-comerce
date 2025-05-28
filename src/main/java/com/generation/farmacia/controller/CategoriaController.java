package com.generation.farmacia.controller;

import com.generation.farmacia.model.Categoria;
import com.generation.farmacia.repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    public ResponseEntity<List<Categoria>> getAll() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getById(@PathVariable Long id) {
        return categoriaRepository.findById(id)
                .map(resposta -> ResponseEntity.ok(resposta))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Categoria>> getByTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(categoriaRepository.findAllByTipoContainingIgnoreCase(tipo));
    }

    // MÉTODO POST ATUALIZADO PARA IMPEDIR DUPLICATAS
    @PostMapping
    public ResponseEntity<Categoria> post(@Valid @RequestBody Categoria categoria) {
        // 1. Verifica se já existe uma categoria com o mesmo tipo (ignorando caixa)
        Optional<Categoria> categoriaExistente = categoriaRepository.findByTipoIgnoreCase(categoria.getTipo());

        // 2. Se a categoria já existe, retorna 409 Conflict
        if (categoriaExistente.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já existente com este tipo!", null);
        }

        // 3. Se não existe, salva a nova categoria
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepository.save(categoria));
    }

    @PutMapping
    public ResponseEntity<Categoria> put(@Valid @RequestBody Categoria categoria) {
        // Para o PUT, também queremos evitar duplicatas de 'tipo', mas permitir atualizar o próprio ID.
        // 1. Verifica se a categoria que se tenta atualizar existe
        if (categoriaRepository.existsById(categoria.getId())) {
            // 2. Verifica se já existe OUTRA categoria com o mesmo tipo (ignorando caixa)
            Optional<Categoria> categoriaExistente = categoriaRepository.findByTipoIgnoreCase(categoria.getTipo());

            // Se existir uma categoria com o mesmo tipo E o ID dela for diferente do ID da categoria que está sendo atualizada
            if (categoriaExistente.isPresent() && categoriaExistente.get().getId() != categoria.getId()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outra categoria com este tipo!", null);
            }

            // 3. Se não houver conflito, salva a atualização
            return ResponseEntity.status(HttpStatus.OK).body(categoriaRepository.save(categoria));
        } else {
            // Se a categoria a ser atualizada não existe, retorna 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);

        if (categoria.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        categoriaRepository.deleteById(id);
    }
}