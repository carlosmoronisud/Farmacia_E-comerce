package com.generation.farmacia.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.farmacia.model.Usuario;
import com.generation.farmacia.model.UsuarioLogin; // DTO para login local
import com.generation.farmacia.repository.UsuarioRepository;
import com.generation.farmacia.security.JwtService;
// Importações para Google Login
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Ou JacksonFactory, dependendo da sua preferência de JSON




@Service // Renomeado de UsuarioService para ServicoDeUsuario na explicação, mas mantenha o nome da classe
public class UsuarioService { // O nome do arquivo e classe continua UsuarioService

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

    // --- NOVO: Para validação do token do Google ---
    // Você precisará configurar o CLIENT_ID do seu projeto Google Cloud aqui.
    // O CLIENT_ID é obtido quando você configura as credenciais OAuth 2.0
    // no Console de Desenvolvedores do Google para sua aplicação web ou Android/iOS.
    // Geralmente, isso é lido de application.properties ou de variáveis de ambiente.
    private static final String CLIENT_ID = "323883270092-ke6bscn6njau06ssposa02vtsto0rdv7.apps.googleusercontent.com"
    		+ ""; 


	public List<Usuario> getAll() {
		return usuarioRepository.findAll();
	}

	public Optional<Usuario> getById(Long id) {
		return usuarioRepository.findById(id);
	}

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		// Validar senha apenas se for cadastro local (sem provedor de autenticação ou se for "LOCAL")
		if (usuario.getProvedorAutenticacao() == null || usuario.getProvedorAutenticacao().equals("LOCAL")) {
			if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é obrigatória para cadastro local!", null);
			}
			if (usuario.getSenha().length() < 8) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter no mínimo 8 caracteres para cadastro local!", null);
			}
		}

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
			return Optional.empty(); // Retorna vazio se o email já estiver cadastrado
		}

        // Criptografar a senha apenas se ela existir (ou seja, não é um login do Google puro)
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(criptografarSenha(usuario.getSenha()));
        } else {
            // Para usuários do Google que não têm senha, garanta que o campo seja nulo ou vazio
            // ou um valor seguro que não cause problemas no banco
            usuario.setSenha(null); // Ou "" se o banco não permitir null
        }

        // Definir o provedor de autenticação padrão se não foi explicitamente setado
        if (usuario.getProvedorAutenticacao() == null) {
            usuario.setProvedorAutenticacao("LOCAL");
        }


		usuario.setId(null); // Garante que o ID será gerado pelo banco

		return Optional.ofNullable(usuarioRepository.save(usuario));
	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {

		if(usuarioRepository.findById(usuario.getId()).isPresent()) {

			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());

			if ( (buscaUsuario.isPresent()) && ( buscaUsuario.get().getId() != usuario.getId()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);

            // Criptografar a senha apenas se uma nova senha for fornecida
            // e o provedor de autenticação for LOCAL
            if (usuario.getSenha() != null && !usuario.getSenha().isBlank() && 
                (usuario.getProvedorAutenticacao() == null || usuario.getProvedorAutenticacao().equals("LOCAL"))) {
                usuario.setSenha(criptografarSenha(usuario.getSenha()));
            } else if (usuario.getProvedorAutenticacao() != null && usuario.getProvedorAutenticacao().equals("GOOGLE")) {
                // Se o usuário é do Google, não atualize a senha, preserve a que está no banco (ou null)
                Usuario usuarioExistente = usuarioRepository.findById(usuario.getId()).get();
                usuario.setSenha(usuarioExistente.getSenha());
            }
			
			return Optional.ofNullable(usuarioRepository.save(usuario));
			
		}

		return Optional.empty();
	}

	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

		var credenciais = new UsernamePasswordAuthenticationToken(usuarioLogin.get().getUsuario(),
				usuarioLogin.get().getSenha());

		Authentication authentication = authenticationManager.authenticate(credenciais);

		if (authentication.isAuthenticated()) {

			Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());

			if (usuario.isPresent()) {
                // Verificar se o usuário autenticado é um usuário local (não Google)
                // para evitar que um usuário do Google tente logar com senha.
                if (usuario.get().getProvedorAutenticacao() != null && usuario.get().getProvedorAutenticacao().equals("GOOGLE")) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Este usuário deve logar via Google!", null);
                }

				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get().setSenha(""); // Limpa a senha antes de retornar
				usuarioLogin.get().setToken(gerarToken(usuarioLogin.get().getUsuario()));

				return usuarioLogin;
			}
		}

		return Optional.empty();
	}

    // --- NOVO MÉTODO: Autenticação via Google ---
    // Retorna UsuarioLogin que inclui o JWT gerado pela sua API
    public Optional<UsuarioLogin> autenticarUsuarioGoogle(String googleIdToken) {
        
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(CLIENT_ID)) // Seu CLIENT_ID
            .build();

        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(googleIdToken); // Tenta verificar o token
        } catch (GeneralSecurityException | IOException e) {
            // Erro na verificação do token (expirado, inválido, etc.)
            System.err.println("Erro ao verificar token do Google: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do Google inválido ou expirado!", e);
        }

        if (idToken != null) {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();

            // Obter informações do usuário do payload do token
            String email = payload.getEmail();
            String nome = (String) payload.get("name");
            String fotoUrl = (String) payload.get("picture");
            String googleId = payload.getSubject(); // ID único do Google para o usuário

            Optional<Usuario> usuarioExistente = usuarioRepository.findByUsuario(email);

            Usuario usuarioProcessado;
            if (usuarioExistente.isPresent()) {
                usuarioProcessado = usuarioExistente.get();
                // Opcional: Atualizar nome e foto caso o Google tenha informações mais recentes
                if (nome != null && !nome.isBlank()) {
                    usuarioProcessado.setNome(nome);
                }
                if (fotoUrl != null && !fotoUrl.isBlank()) {
                    usuarioProcessado.setFoto(fotoUrl);
                }
                // Garante que o provedor seja Google e o ID externo esteja correto
                usuarioProcessado.setProvedorAutenticacao("GOOGLE");
                usuarioProcessado.setIdProvedorExterno(googleId);
                usuarioRepository.save(usuarioProcessado); // Salva atualizações
            } else {
                // Usuário não existe, vamos cadastrá-lo
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome);
                novoUsuario.setUsuario(email);
                novoUsuario.setFoto(fotoUrl);
                novoUsuario.setProvedorAutenticacao("GOOGLE");
                novoUsuario.setIdProvedorExterno(googleId);
                // Não há senha para usuários Google, então o campo 'senha' ficará nulo/vazio
                novoUsuario.setSenha(null); // Garante que a senha não seja setada para usuários Google

                usuarioProcessado = usuarioRepository.save(novoUsuario); // Salva o novo usuário
            }

            // Gerar o JWT da sua aplicação para o usuário autenticado via Google
            UsuarioLogin respostaLogin = new UsuarioLogin();
            respostaLogin.setId(usuarioProcessado.getId());
            respostaLogin.setNome(usuarioProcessado.getNome());
            respostaLogin.setUsuario(usuarioProcessado.getUsuario());
            respostaLogin.setFoto(usuarioProcessado.getFoto());
            respostaLogin.setToken(gerarToken(usuarioProcessado.getUsuario())); // Gerar token com o email

            return Optional.of(respostaLogin);

        } else {
            // Token inválido (não pode ser verificado pelo Google)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não foi possível validar o token do Google.", null);
        }
    }


	private String gerarToken(String usuario) {
		return "Bearer " + jwtService.generateToken(usuario);
	}

	private String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(senha);
	}
}