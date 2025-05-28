package com.generation.farmacia.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; 


@Entity
@Table(name = "tb_produtos") // Define o nome da tabela no banco de dados
public class Produto {

    @Id // Define que este é o atributo de chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática de ID
    private Long id;

    @NotBlank(message = "O atributo 'nome' é obrigatório!")
    @Size(min = 3, max = 255, message = "O atributo 'nome' deve ter no mínimo 3 e no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "O atributo 'descricao' é obrigatório!")
    @Size(min = 5, max = 1000, message = "O atributo 'descricao' deve ter no mínimo 5 e no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "O atributo 'preco' é obrigatório!")
    // Usamos BigDecimal para preços para evitar problemas de precisão com números de ponto flutuante
    private BigDecimal preco;

    @NotNull(message = "O atributo 'quantidade' é obrigatório!")
    private Integer quantidade; 

    // --- Relacionamento Muitos para Um (ManyToOne) com Categoria ---
    @ManyToOne // Indica que muitos produtos podem ter uma categoria
    @JsonIgnoreProperties("produtos") // Evita loop infinito na serialização JSON (aqui 'produtos' é o nome da lista em Categoria)
    private Categoria categoria; // Atributo que representa a categoria à qual o produto pertence

    // Construtor padrão (necessário para JPA)
    public Produto() {}

    // --- Getters e Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}