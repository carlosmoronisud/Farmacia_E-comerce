package com.generation.farmacia.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Importe este
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.generation.farmacia.security.jwt.JwtAuthFilter;



@Configuration
@EnableWebSecurity
public class BasicSecurityConfig {

    @Autowired
    private AuthenticationSuccessHandler oauth2LoginSuccessHandler;

    @Autowired
    private JwtAuthFilter jwtAuthFilter; 

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
            .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/usuarios/logar").permitAll()
                .requestMatchers("/usuarios/cadastrar").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers("/error/**").permitAll()
                .requestMatchers("/").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2LoginSuccessHandler))
            .httpBasic(withDefaults())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Adicione o filtro JWT

        return http.build();
    }
}