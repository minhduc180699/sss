package com.sss.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for Keycloak integration
 * 
 * @author : Ducpm56
 * @date : 07/08/2025
 **/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/public/**").permitAll()
                .requestMatchers("/api/test/health").permitAll()
                .requestMatchers("/api/test/connection").permitAll()
                .requestMatchers("/api/test/login-test").permitAll()
                .requestMatchers("/api/test/users").permitAll()
                .requestMatchers("/api/test/user/**").permitAll()
                .requestMatchers("/api/test/update-user/**").permitAll()
                // Auth test endpoint - require authentication
                .requestMatchers("/api/test/auth-test").authenticated()
                .requestMatchers("/graphql").permitAll()
                .requestMatchers("/graphiql/**").permitAll()
                .requestMatchers("/subscriptions").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Protected endpoints
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/auth/profile").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            java.util.Set<org.springframework.security.core.GrantedAuthority> authorities = new java.util.HashSet<>();
            
            // Extract roles from realm_access.roles
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> realmAccessMap = (java.util.Map<String, Object>) realmAccess;
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> rolesList = (java.util.List<String>) roles;
                    for (String role : rolesList) {
                        // Add role as-is if it already has ROLE_ prefix, otherwise add prefix
                        if (role.startsWith("ROLE_")) {
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
                        } else {
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                }
            }
            
            System.out.println("🔑 JWT Claims - realm_access: " + realmAccess);
            System.out.println("🔑 Extracted authorities: " + authorities);
            return authorities;
        });
        
        // Set the principal name claim
        authenticationConverter.setPrincipalClaimName("preferred_username");
        
        return authenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8080/realms/sss-realm/protocol/openid-connect/certs").build();
    }
}
