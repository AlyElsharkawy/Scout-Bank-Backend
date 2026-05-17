package org.sportingscout.scout_bank_backend.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(1) // Ensures Spring Security evaluates this rule first
  public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
        .authorizeHttpRequests(auth -> auth
            .anyRequest().hasAuthority("article:write"))
        .httpBasic(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable());

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // HTTP BASIC AUTHENTICATION IS CURRENTLY USED
    // THIS WILL BE REPLACED FOR SESSION COOKIES LATER
    // FURTHERMORE, CSRF WILL BE DISABLED WHEN HTTP BASIC AUTHENTICATION IS ENABLED
    http
        .httpBasic(basic -> {
        })
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/ranks").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/ranks").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/roles").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/roles").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/organizations").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/organizations").permitAll()
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html")
            .permitAll()
            .anyRequest().authenticated());

    return http.build();
  }
}
