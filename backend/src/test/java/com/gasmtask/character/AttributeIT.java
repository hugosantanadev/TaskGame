package com.gasmtask.character;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

/** Atributos de RPG: as tarefas concluídas treinam o atributo da categoria e sobem o nível dele. */
class AttributeIT extends IntegrationTest {

    @Test
    void estudarTreinaInteligenciaESobeDeNivel() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        for (int i = 0; i < 5; i++) {   // taskJson cria missões de Estudo
            api.createTask(token, taskJson("Estudo " + i, "MANDATORY", List.of("MONDAY"), "1%d:00".formatted(i), true));
        }
        List<String> ids = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[*].id");

        String last = null;
        for (String id : ids) {
            last = api.complete(token, id).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        }

        // Cinco obrigatórias de 5 pontos: 25 XP de Inteligência, o suficiente para o nível 2
        assertThat(JsonPath.<String>read(last, "$.attribute.status.attribute")).isEqualTo("INTELLIGENCE");
        assertThat(JsonPath.<Integer>read(last, "$.attribute.gained")).isEqualTo(5);
        assertThat((Boolean) JsonPath.read(last, "$.attribute.leveledUp")).isTrue();
        assertThat(JsonPath.<Integer>read(last, "$.attribute.status.level")).isEqualTo(2);

        String character = api.getJson(token, "/api/v1/character");
        assertThat(JsonPath.<List<Object>>read(character, "$.attributes")).hasSize(7);
        assertThat(JsonPath.<List<Integer>>read(character, "$.attributes[?(@.attribute == 'INTELLIGENCE')].xp"))
                .containsExactly(25);
        assertThat(JsonPath.<List<Integer>>read(character, "$.attributes[?(@.attribute == 'STRENGTH')].level"))
                .containsExactly(1);
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/me/game-state"),
                "$.character.attributes.INTELLIGENCE")).isEqualTo(2);
    }
}
