// src/main/java/com/generation/blogpessoal/security/BasicSecurityConfig.java
package com.generation.blogpessoal.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Importe esta classe
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// Imports necessários para o ClientRegistrationRepository manual
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames; // Opcional, para nome de atributo de usuário
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.generation.blogpessoal.security.jwt.JwtAuthFilter;
import com.generation.blogpessoal.user.UserDetailsServiceImpl;


@Configuration
@EnableWebSecurity 
@EnableMethodSecurity(prePostEnabled = true) 
public class BasicSecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter; 

    @Autowired
    private OAuth2AuthenticationException oauth2AuthenticationSuccessHandler; 

    // Valores injetados do application.properties para o ClientRegistration
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    // **NOVO BEAN**: Definindo ClientRegistrationRepository manualmente
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Retorna um repositório em memória com os clientes que você define
        // Aqui, definimos apenas o Google
        return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    }

    // Método auxiliar para criar o ClientRegistration do Google
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google") // ID de registro (usado na URL /oauth2/authorization/google)
                .clientId(googleClientId) // Injetado do application.properties
                .clientSecret(googleClientSecret) // Injetado do application.properties
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}") // Padrão de redirecionamento
                .scope("openid", "profile", "email") // Escopos de permissão solicitados
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth") // URL de autorização do Google
                .tokenUri("https://oauth2.googleapis.com/token") // URL para obter o token de acesso
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo") // URL para obter dados do usuário
                .userNameAttributeName(IdTokenClaimNames.SUB) // Atributo que identifica o usuário no token de ID (Google usa 'sub')
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs") // URL para chaves públicas JWK
                .clientName("Google") // Nome para exibição (ex: no botão de login)
                .build();
    }
    
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
            .csrf(csrf -> csrf.disable()) 
            .cors(withDefaults());

        http
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2AuthenticationSuccessHandler) 
                .failureUrl("/oauth2/loginFailure") 
            );
        
        http
            .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) 
                .sessionFixation().migrateSession() 
            )
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/usuarios/logar").permitAll()
                .requestMatchers("/usuarios/cadastrar").permitAll()
                .requestMatchers(HttpMethod.OPTIONS).permitAll() 
                .requestMatchers("/error/**").permitAll() 
                .requestMatchers("/").permitAll() 

                .requestMatchers("/oauth2/authorization/**").permitAll() 
                .requestMatchers("/login/oauth2/code/**").permitAll() 
                .requestMatchers("/oauth2/finalizarLoginSocial").permitAll() 
                .requestMatchers("/oauth2/loginFailure").permitAll() 

                .anyRequest().authenticated() 
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(withDefaults()); 

        return http.build();
    }
}