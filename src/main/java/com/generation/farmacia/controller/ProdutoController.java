package com.generation.farmacia.controller;

import com.generation.farmacia.model.Categoria;
import com.generation.farmacia.model.Produto;
import com.generation.farmacia.repository.CategoriaRepository;
import com.generation.farmacia.repository.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos") // Endpoint base para Produtos
@CrossOrigin(origins = "*", allowedHeaders = "*") // Permite CORS
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository; // Injeta o repositório de Produto

    @Autowired
    private CategoriaRepository categoriaRepository; // Injeta o repositório de Categoria para validar relacionamento

    // --- Métodos CRUD ---

    // 1. GET para buscar todos os produtos
    @GetMapping
    public ResponseEntity<List<Produto>> getAll() {
        return ResponseEntity.ok(produtoRepository.findAll());
    }

    // 2. GET para buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable Long id) {
        return produtoRepository.findById(id)
                .map(resposta -> ResponseEntity.ok(resposta))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 3. GET para buscar produtos por 'nome'
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Produto>> getByNome(@PathVariable String nome) {
        return ResponseEntity.ok(produtoRepository.findAllByNomeContainingIgnoreCase(nome));
    }

    // 4. POST para criar um novo produto
    @PostMapping
    public ResponseEntity<Produto> post(@Valid @RequestBody Produto produto) {
        // Antes de salvar o produto, verifica se a categoria associada existe
        if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da Categoria é obrigatório!", null);
        }

        Optional<Categoria> categoriaExistente = categoriaRepository.findById(produto.getCategoria().getId());

        if (categoriaExistente.isPresent()) {
            // Se a categoria existe, salva o produto
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoRepository.save(produto));
        } else {
            // Se a categoria não existe, retorna 404 Not Found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada!", null);
        }
    }

    // 5. PUT para atualizar um produto existente
    @PutMapping
    public ResponseEntity<Produto> put(@Valid @RequestBody Produto produto) {
        // Verifica se o produto existe
        if (produtoRepository.existsById(produto.getId())) {
            // Se o produto está sendo atualizado, a categoria também deve ser válida
            if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da Categoria é obrigatório!", null);
            }

            Optional<Categoria> categoriaExistente = categoriaRepository.findById(produto.getCategoria().getId());

            if (categoriaExistente.isPresent()) {
                // Se o produto e a categoria existem, salva a atualização
                return ResponseEntity.status(HttpStatus.OK).body(produtoRepository.save(produto));
            } else {
                // Se a categoria não existe, retorna 404 Not Found
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada!", null);
            }
        } else {
            // Se o produto a ser atualizado não existe, retorna 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 6. DELETE para apagar um produto por ID
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Optional<Produto> produto = produtoRepository.findById(id);

        if (produto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        produtoRepository.deleteById(id);
    }
}