package com.generation.farmacia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importe JsonIgnoreProperties
import jakarta.persistence.CascadeType; // Importe CascadeType
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany; // Importe OneToMany
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List; // Importe List

@Entity
@Table(name = "tb_categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O atributo 'tipo' é obrigatório!")
    @Size(min = 3, max = 100, message = "O atributo 'tipo' deve ter no mínimo 3 e no máximo 100 caracteres")
    @Column(unique = true)
    private String tipo;

    // --- Relacionamento Um para Muitos (OneToMany) com Produto ---
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL) // 'mappedBy' indica que a relação é gerenciada pelo campo 'categoria' em Produto
    @JsonIgnoreProperties("categoria") // Evita loop infinito na serialização JSON (aqui 'categoria' é o nome do atributo em Produto)
    private List<Produto> produtos; // Lista de produtos associados a esta categoria

    public Categoria() {}

    // --- Getters e Setters (Certifique-se de ter para a lista de produtos) ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }
}