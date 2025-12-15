package com.aiscientist.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Server.
 *
 * This configuration uses HTTP Basic authentication which is stateless.
 * CSRF protection is disabled because:
 * 1. Eureka clients communicate via REST APIs, not browser-based forms
 * 2. HTTP Basic auth doesn't use cookies/sessions, making CSRF attacks inapplicable
 * 3. All Eureka endpoints require authentication via Authorization header
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("java:S4502") // CSRF not applicable for stateless API with Basic auth
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disabled: This API uses stateless HTTP Basic auth, not cookie-based sessions
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
