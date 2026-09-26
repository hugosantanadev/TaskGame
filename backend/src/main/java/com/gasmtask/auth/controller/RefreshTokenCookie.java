package com.gasmtask.auth.controller;

import java.time.Duration;
import java.util.Optional;

import com.gasmtask.auth.config.RefreshTokenProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/** Monta, lê e apaga o cookie do refresh token com os mesmos atributos em todos os casos. */
@Component
public class RefreshTokenCookie {

    private final RefreshTokenProperties properties;

    public RefreshTokenCookie(RefreshTokenProperties properties) {
        this.properties = properties;
    }

    public String create(String rawToken) {
        return base(rawToken).maxAge(properties.ttl()).build().toString();
    }

    public String clear() {
        return base("").maxAge(Duration.ZERO).build().toString();
    }

    public Optional<String> read(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, properties.cookieName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank());
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.cookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Strict")
                .path(properties.cookiePath());
    }
}
