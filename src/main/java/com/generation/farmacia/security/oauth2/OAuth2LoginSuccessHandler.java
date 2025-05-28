package com.generation.farmacia.security.oauth2; // ATENÇÃO: pacote correto

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generation.farmacia.model.Usuario; // ATENÇÃO: pacote correto
import com.generation.farmacia.repository.UsuarioRepository; // ATENÇÃO: pacote correto
import com.generation.farmacia.service.JwtService; // ATENÇÃO: pacote correto
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${frontend.redirect.url:http://localhost:8080/}")
    private String frontendRedirectUrl;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String username;
        String fullName = null;
        String pictureUrl = null;

        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            username = oidcUser.getEmail();
            fullName = oidcUser.getFullName();
            pictureUrl = oidcUser.getPicture();
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            username = oauth2User.getAttribute("email");
            if (username == null) {
                username = oauth2User.getName();
            }
            fullName = oauth2User.getAttribute("name");
            pictureUrl = oauth2User.getAttribute("picture");
        } else {
            username = authentication.getName();
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(username);
        Usuario usuario;

        if (optionalUsuario.isPresent()) {
            usuario = optionalUsuario.get();
            usuario.setNome(fullName != null ? fullName : usuario.getNome());
            usuario.setFoto(pictureUrl != null ? pictureUrl : usuario.getFoto());
        } else {
            usuario = new Usuario();
            usuario.setEmail(username);
            usuario.setNome(fullName != null ? fullName : username);
            usuario.setFoto(pictureUrl);
            usuario.setSenha(UUID.randomUUID().toString());
            usuario.setRole("ROLE_USER");
        }
        usuarioRepository.save(usuario);

        String jwtToken = jwtService.generateToken(username);

        String redirectUrlWithToken = frontendRedirectUrl + "?token=" + jwtToken;
        response.sendRedirect(redirectUrlWithToken);
    }
}