package com.generation.farmacia.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // IMPORT CORRETO para @EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // IMPORT para WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // IMPORT para AntPathRequestMatcher

import com.generation.farmacia.security.jwt.JwtAuthFilter; // ATENÇÃO: pacote correto
import com.generation.farmacia.security.oauth2.OAuth2LoginSuccessHandler; // ATENÇÃO: IMPORT CORRETO para seu handler customizado


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita o uso de @PreAuthorize e @PostAuthorize
public class BasicSecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler; 

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Bean para ignorar o Swagger UI da segurança
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui.html")
        );
    }
     
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
            .authorizeHttpRequests((auth) -> auth
                // Endpoints públicos (sem autenticação)
                .requestMatchers("/usuarios/logar").permitAll()
                .requestMatchers("/usuarios/cadastrar").permitAll()
                .requestMatchers("/oauth2/**").permitAll() // Para fluxo de login OAuth2
                .requestMatchers(HttpMethod.OPTIONS).permitAll() // Necessário para CORS
                .requestMatchers("/error/**").permitAll() // Para erros
                .requestMatchers("/").permitAll() // Página inicial, se houver

                // Agora, todos os endpoints que não estão explicitamente permitidos acima,
                // e que não são ignorados pelo webSecurityCustomizer, exigirão autenticação.
                // As regras de @PreAuthorize nos controllers vão filtrar ainda mais por role.
                .anyRequest().authenticated() // TUDO O MAIS VAI EXIGIR AUTENTICAÇÃO AGORA!
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2LoginSuccessHandler)) // Usa seu handler customizado
            .httpBasic(withDefaults()) // Habilita autenticação HTTP Basic (opcional, pode remover se usar apenas JWT/OAuth2)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Adiciona o filtro JWT

        return http.build();
    }
}