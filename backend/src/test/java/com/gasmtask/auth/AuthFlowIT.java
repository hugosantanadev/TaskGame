package com.gasmtask.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;

import com.gasmtask.support.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthFlowIT extends IntegrationTest {

    @Test
    void registerCreatesAccountAndOpensSession() throws Exception {
        String email = "Nova.Pessoa-" + UUID.randomUUID() + "@GasmTask.test";

        mvc.perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("  Nova Pessoa ", email, DEFAULT_PASSWORD, "America/Recife")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, ME))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.email").value(email.toLowerCase(Locale.ROOT)))
                .andExpect(jsonPath("$.user.displayName").value("Nova Pessoa"))
                .andExpect(jsonPath("$.user.timeZone").value("America/Recife"))
                .andExpect(jsonPath("$.user.rankingVisible").value(true))
                .andExpect(cookie().exists(REFRESH_COOKIE))
                .andExpect(cookie().httpOnly(REFRESH_COOKIE, true))
                .andExpect(cookie().path(REFRESH_COOKIE, "/api/v1/auth"))
                .andExpect(cookie().sameSite(REFRESH_COOKIE, "Strict"));
    }

    @Test
    void registerRejectsEmailAlreadyInUseIgnoringCase() throws Exception {
        RegisteredUser existing = registerUser();

        mvc.perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Outra Pessoa", existing.email().toUpperCase(Locale.ROOT),
                                "outra-senha-123", "America/Sao_Paulo")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void registerReportsEachInvalidField() throws Exception {
        mvc.perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("A", "nao-e-email", "curta", "Marte/Olympus")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field",
                        containsInAnyOrder("displayName", "email", "password", "timeZone")));
    }

    @Test
    void malformedJsonIsReportedAsProblem() throws Exception {
        mvc.perform(post(REGISTER).contentType(MediaType.APPLICATION_JSON).content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void loginGivesTheSameAnswerForWrongPasswordAndUnknownEmail() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user.email(), "senha-errada-123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        mvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("ninguem-" + UUID.randomUUID() + "@gasmtask.test", "senha-errada-123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginOpensSessionThatReachesProtectedEndpoints() throws Exception {
        RegisteredUser user = registerUser();

        MvcResult login = mvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user.email().toUpperCase(Locale.ROOT), user.password())))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(REFRESH_COOKIE))
                .andReturn();

        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, bearer(accessTokenOf(login))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void authRoutesIgnoreAStaleAccessToken() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(post(LOGIN)
                        .header(HttpHeaders.AUTHORIZATION, bearer("token-vencido-ou-invalido"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user.email(), user.password())))
                .andExpect(status().isOk());

        mvc.perform(post(REFRESH)
                        .header(HttpHeaders.AUTHORIZATION, bearer("token-vencido-ou-invalido"))
                        .cookie(refreshCookie(user.refreshToken())))
                .andExpect(status().isOk());
    }

    @Test
    void refreshRotatesTheTokenAndRevokesTheSessionWhenAnOldTokenReturns() throws Exception {
        RegisteredUser user = registerUser();

        MvcResult refreshed = mvc.perform(post(REFRESH).cookie(refreshCookie(user.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(user.email()))
                .andReturn();
        String rotated = refreshTokenOf(refreshed);
        assertThat(rotated).isNotEqualTo(user.refreshToken());

        // O token antigo reapareceu: tratado como cópia indevida, a sessão inteira cai.
        mvc.perform(post(REFRESH).cookie(refreshCookie(user.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
                .andExpect(cookie().maxAge(REFRESH_COOKIE, 0));

        mvc.perform(post(REFRESH).cookie(refreshCookie(rotated)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(post(LOGOUT).cookie(refreshCookie(user.refreshToken())))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(REFRESH_COOKIE, 0));

        mvc.perform(post(REFRESH).cookie(refreshCookie(user.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutCookieIsRejected() throws Exception {
        mvc.perform(post(REFRESH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void protectedEndpointsAnswerWithProblemDetailsWhenTheTokenIsMissingOrInvalid() throws Exception {
        mvc.perform(get(ME))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, "Bearer nao-e-um-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
