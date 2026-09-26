package com.gasmtask.shared.security;

import java.util.List;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/** Converte o JWT validado em uma autenticação cujo principal é o {@link AuthenticatedUser}. */
public class AuthenticatedUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AuthenticatedUser principal = new AuthenticatedUser(UUID.fromString(jwt.getSubject()));
        return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, List.of());
    }
}
