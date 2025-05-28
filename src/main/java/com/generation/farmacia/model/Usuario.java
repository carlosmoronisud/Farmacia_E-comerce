package com.generation.farmacia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column; // Adicione este import para @Column
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O atributo Nome é obrigatório!")
    @Size(min = 3, max = 255, message = "O atributo Nome deve ter no mínimo 3 e no máximo 255 caracteres")
    private String nome;

    @NotBlank(message = "O atributo E-mail é obrigatório!")
    @Email(message = "O atributo E-mail deve ser um e-mail válido!")
    @Column(unique = true) // Importante: Garante que o e-mail seja único no banco de dados
    private String email;

    @NotBlank(message = "O atributo Senha é obrigatório!")
    @Size(min = 8, message = "A Senha deve ter no mínimo 8 caracteres")
    @JsonIgnore
    private String senha;

    private String foto;

    // NOVO CAMPO PARA O PAPEL/ROLE
    @NotBlank(message = "O atributo Role é obrigatório!") // Adiciona validação para a role
    private String role; // Ex: "ROLE_USER", "ROLE_ADMIN"

    // Construtor padrão (necessário para JPA)
    public Usuario() {}

    // Construtor para usuários OAuth2 - AGORA COM O PARÂMETRO 'role'
    public Usuario(String nome, String email, String foto, String role) {
        this.nome = nome;
        this.email = email;
        this.foto = foto;
        this.senha = "oauth2_generated_password_for_google_user_1234567890"; // Senha placeholder
        this.role = role; // Define o papel aqui
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    // NOVO GETTER E SETTER PARA ROLE
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // --- Métodos da interface UserDetails: Atualizado para usar o campo 'role' ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna a role do usuário. Spring Security espera que roles comecem com "ROLE_"
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}