package com.gasmtask.user;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gasmtask.support.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class MeControllerIT extends IntegrationTest {

    @Test
    void returnsTheProfileOfTheTokenOwner() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.displayName").value("Pessoa Teste"))
                .andExpect(jsonPath("$.timeZone").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.rankingVisible").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void updatesOnlyTheFieldsThatWereSent() throws Exception {
        RegisteredUser user = registerUser();
        String auth = bearer(user.accessToken());

        mvc.perform(patch(ME).header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "Hugo", "rankingVisible": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Hugo"))
                .andExpect(jsonPath("$.rankingVisible").value(false))
                .andExpect(jsonPath("$.timeZone").value("America/Sao_Paulo"));

        mvc.perform(patch(ME).header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timeZone": "Europe/Lisbon"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("Europe/Lisbon"))
                .andExpect(jsonPath("$.displayName").value("Hugo"));

        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.displayName").value("Hugo"))
                .andExpect(jsonPath("$.timeZone").value("Europe/Lisbon"))
                .andExpect(jsonPath("$.rankingVisible").value(false));
    }

    @Test
    void rejectsBlankNameAndUnknownTimeZone() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(patch(ME).header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "   ", "timeZone": "Lua/Base_Alfa"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("displayName", "timeZone")));
    }
}
