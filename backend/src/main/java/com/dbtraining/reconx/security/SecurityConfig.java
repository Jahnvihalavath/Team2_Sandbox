package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/auth/login",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2/**"
                        ).permitAll()

                        // Trade APIs
                        .requestMatchers(HttpMethod.GET, "/v1/trades/**")
                        .hasAnyRole("VIEWER", "TRADER", "RECON_ANALYST", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/trades")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/v1/trades/**")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/v1/trades/**")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/v1/trades/**")
                        .hasRole("ADMIN")

                        // Recon APIs
                        .requestMatchers("/v1/recon/**")
                        .hasAnyRole("RECON_ANALYST", "ADMIN")

                        // Audit APIs
                        .requestMatchers("/v1/audit/**")
                        .hasAnyRole("RECON_ANALYST", "ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated())

                // Allow H2 Console
                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable()))

                // Register JWT Filter
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}