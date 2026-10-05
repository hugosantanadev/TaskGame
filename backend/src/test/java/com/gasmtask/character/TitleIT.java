package com.gasmtask.character;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Títulos: ganhos pelo nível do atributo, avisados na conclusão e escolhidos para aparecer no perfil e ranking. */
class TitleIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void tituloGanhoNoNivelAvisaNaConclusaoEPodeSerEscolhido() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        UUID userId = UUID.fromString(JsonPath.read(api.getJson(token, "/api/v1/me"), "$.id"));

        mvc.perform(put("/api/v1/character/title").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"title\": \"STUDENT\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("TITLE_LOCKED"));

        // Atalho de teste: 70 XP de Estudo já feitos; a próxima conclusão (5) leva ao nível 3 (75)
        api.createTask(token, taskJson("Estudar Java", "MANDATORY", List.of("MONDAY"), "20:00", true));
        String today = api.getJson(token, "/api/v1/today");
        String id = JsonPath.read(today, "$.occurrences[0].id");
        new JdbcTemplate(dataSource).update("""
                INSERT INTO task_occurrences (id, user_id, plan_id, task_id, occurrence_date, title, category, kind,
                    points, base_coins, requires_proof, status, completed_at, on_time, earned_points, earned_coins,
                    proof_attached, created_at, version)
                SELECT gen_random_uuid(), user_id, plan_id, NULL, occurrence_date - 1, 'Estudo antigo', 'STUDY',
                    'EXTRA', 70, 1, FALSE, 'COMPLETED', now(), FALSE, 70, 1, FALSE, now(), 0
                FROM task_occurrences WHERE id = ?::uuid
                """, id);

        String completion = api.complete(token, id).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(JsonPath.<Integer>read(completion, "$.attribute.status.level")).isEqualTo(3);
        assertThat(JsonPath.<List<String>>read(completion, "$.attribute.unlockedTitles")).containsExactly("STUDENT");

        mvc.perform(put("/api/v1/character/title").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"title\": \"STUDENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeTitle").value("STUDENT"));
        String character = api.getJson(token, "/api/v1/character");
        assertThat(JsonPath.<List<Object>>read(character, "$.titles")).hasSize(21);
        assertThat(JsonPath.<List<Boolean>>read(character, "$.titles[?(@.code == 'STUDENT')].unlocked"))
                .containsExactly(true);
        assertThat(JsonPath.<String>read(api.getJson(token, "/api/v1/me"), "$.activeTitle")).isEqualTo("STUDENT");
        assertThat(JsonPath.<List<String>>read(api.getJson(token, "/api/v1/rankings?size=100"),
                "$.entries.content[?(@.you == true)].title")).containsExactly("STUDENT");

        mvc.perform(put("/api/v1/character/title").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"title\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeTitle").doesNotExist());
        assertThat(userId).isNotNull();
    }
}
