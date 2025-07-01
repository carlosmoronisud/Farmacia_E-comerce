package com.generation.farmacia.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank; // Manter para nome e usuario
import jakarta.validation.constraints.Size; // Manter para foto

@Entity
@Table(name = "tb_usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "O Atributo Nome é Obrigatório!")
	private String nome;

	@Schema(example = "email@email.com.br")
	@NotBlank(message = "O Atributo Usuário é Obrigatório!")
	@Email(message = "O Atributo Usuário deve ser um email válido!")
	private String usuario;

	// ANTES:
	// @NotBlank(message = "O Atributo Senha é Obrigatório!")
	// @Size(min = 8, message = "A Senha deve ter no mínimo 8 caracteres")
	// AGORA:
	// A senha se tornar um campo opcional na model, e o serviço cadastrarUsuario e atualizarUsuario 
	// serao responsáveis por validar a senha apenas se o provedorAutenticacao for "LOCAL".
	@Column(nullable = true) // <-- Adicione esta linha
	private String senha;

	@Size(max = 5000, message = "O link da foto não pode ser maior do que 5000 caracteres")
	private String foto;
	
	private String provedorAutenticacao;
	private String idProvedorExterno;

	// ... Getters e Setters ...

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUsuario() {
		return this.usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getSenha() {
		return this.senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getFoto() {
		return this.foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}
	
	
	public String getProvedorAutenticacao() {
		return provedorAutenticacao;
	}

	public void setProvedorAutenticacao(String provedorAutenticacao) {
		this.provedorAutenticacao = provedorAutenticacao;
	}

	public String getIdProvedorExterno() {
		return idProvedorExterno;
	}

	public void setIdProvedorExterno(String idProvedorExterno) {
		this.idProvedorExterno = idProvedorExterno;
	}
}