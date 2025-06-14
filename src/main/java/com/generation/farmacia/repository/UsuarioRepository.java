package com.generation.farmacia.repository; 

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.farmacia.model.Usuario; 


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}