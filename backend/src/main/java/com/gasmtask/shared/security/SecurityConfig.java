package com.gasmtask.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    private static final String AUTH_PATH = "/api/v1/auth/";
    private static final String[] API_DOCS = {"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};

    /*
     * API stateless: sem sessão HTTP e sem CSRF (a autenticação vem no header Authorization).
     * O único cookie é o de refresh: HttpOnly, SameSite=Strict e restrito ao caminho /api/v1/auth.
     */
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtDecoder jwtDecoder,
                                    ProblemDetailsSecurityHandler problemHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, AUTH_PATH + "**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(API_DOCS).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(new AuthenticatedUserConverter()))
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler));
        return http.build();
    }

    /**
     * Nas rotas de autenticação o header Authorization é ignorado: um access token vencido
     * (por exemplo, esquecido no Swagger) não pode impedir cadastro, login ou renovação.
     */
    static BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver standard = new DefaultBearerTokenResolver();
        return request -> request.getRequestURI().startsWith(AUTH_PATH) ? null : standard.resolve(request);
    }

    /** BCrypt por padrão, com prefixo de algoritmo ({bcrypt}) para permitir migração futura de hash. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
