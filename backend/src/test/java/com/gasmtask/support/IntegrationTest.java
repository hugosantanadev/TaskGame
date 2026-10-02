package com.gasmtask.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Base dos testes de integração: aplicação completa + PostgreSQL real (Testcontainers) + MockMvc
 * passando pela cadeia de filtros do Spring Security. O contexto é reaproveitado entre as classes.
 * Cada teste cria o próprio usuário com e-mail único, então não há limpeza de banco entre testes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({PostgresTestcontainer.class, TestClockConfig.class})
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String REGISTER = "/api/v1/auth/register";
    protected static final String LOGIN = "/api/v1/auth/login";
    protected static final String REFRESH = "/api/v1/auth/refresh";
    protected static final String LOGOUT = "/api/v1/auth/logout";
    protected static final String ME = "/api/v1/me";
    protected static final String REFRESH_COOKIE = "gt_refresh";
    protected static final String DEFAULT_PASSWORD = "senha-forte-123";

    @Autowired
    protected MockMvc mvc;

    /** Relógio da aplicação nos testes: começa no horário real e só pode ser adiantado. */
    @Autowired
    protected TestClock clock;

    @AfterEach
    void resetClock() {
        clock.reset();
    }

    protected RegisteredUser registerUser() throws Exception {
        String email = "pessoa-" + UUID.randomUUID() + "@gasmtask.test";
        MvcResult result = mvc.perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Pessoa Teste", email, DEFAULT_PASSWORD, "America/Sao_Paulo")))
                .andExpect(status().isCreated())
                .andReturn();
        return new RegisteredUser(email, DEFAULT_PASSWORD, accessTokenOf(result), refreshTokenOf(result));
    }

    protected static String registerJson(String displayName, String email, String password, String timeZone) {
        return """
                {"displayName": "%s", "email": "%s", "password": "%s", "timeZone": "%s"}
                """.formatted(displayName, email, password, timeZone);
    }

    protected static String loginJson(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    protected static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    protected static Cookie refreshCookie(String value) {
        return new Cookie(REFRESH_COOKIE, value);
    }

    protected static String accessTokenOf(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.accessToken");
    }

    protected static String refreshTokenOf(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        if (cookie == null) {
            throw new AssertionError("A resposta não trouxe o cookie " + REFRESH_COOKIE);
        }
        return cookie.getValue();
    }

    public record RegisteredUser(String email, String password, String accessToken, String refreshToken) {
    }
}
