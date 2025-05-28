package com.generation.farmacia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity // Define esta classe como uma entidade JPA (tabela no banco de dados)
@Table(name = "tb_categorias") // Define o nome da tabela no banco de dados
public class Categoria {

    @Id // Define que este é o atributo de chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática de ID (auto-incremento)
    private Long id;

    @NotBlank(message = "O atributo 'tipo' é obrigatório!") // Validação: não pode ser nulo ou vazio
    @Size(min = 3, max = 100, message = "O atributo 'tipo' deve ter no mínimo 3 e no máximo 100 caracteres") // Validação de tamanho
    private String tipo;

    // Construtor padrão (necessário para JPA)
    public Categoria() {}

    // --- Getters e Setters ---
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
}