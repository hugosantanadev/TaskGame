package com.gasmtask.user;

import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

/** A conta como produto: plano, exportação dos dados e exclusão com confirmação de senha. */
class AccountIT extends IntegrationTest {

    @Test
    void contaNasceNoPlanoGratisEExportaTudoSemSegredos() throws Exception {
        RegisteredUser user = registerUser();
        PlanningApi api = new PlanningApi(mvc);
        api.createTask(user.accessToken(), taskJson("Academia", "MANDATORY", List.of("MONDAY"), "18:00", false));

        assertThat(JsonPath.<String>read(api.getJson(user.accessToken(), "/api/v1/me"), "$.plan")).isEqualTo("FREE");
        String export = mvc.perform(get("/api/v1/me/export").header(AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"gasmtask-meus-dados.json\""))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(JsonPath.<String>read(export, "$.profile.email")).isEqualTo(user.email());
        assertThat(JsonPath.<List<String>>read(export, "$.missions[*].name")).containsExactly("Academia");
        assertThat(JsonPath.<List<Object>>read(export, "$.missions[0].schedule")).hasSize(1);
        assertThat(export).doesNotContain("password").doesNotContain("token_hash");
    }

    @Test
    void excluirPedeASenhaEApagaAConta() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(delete("/api/v1/me").header(AUTHORIZATION, bearer(user.accessToken()))
                        .contentType(APPLICATION_JSON).content("{\"password\": \"senha-errada\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("PASSWORD_MISMATCH"));

        mvc.perform(delete("/api/v1/me").header(AUTHORIZATION, bearer(user.accessToken()))
                        .contentType(APPLICATION_JSON).content("{\"password\": \"%s\"}".formatted(user.password())))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));

        mvc.perform(post(LOGIN).contentType(APPLICATION_JSON).content(loginJson(user.email(), user.password())))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(ME).header(AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isNotFound());
    }
}
