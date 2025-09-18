package com.store.erp.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite todo
                )
                .formLogin(login -> login.disable()) // Desactiva formulario login
                .httpBasic(basic -> basic.disable()); // Desactiva autenticación básica

        return http.build();
    }
}
