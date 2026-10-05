package com.gasmtask.stats;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

/** Evolução por missão: quantas vezes a academia foi feita por semana e a sequência dela. */
class MissionStatsIT extends IntegrationTest {

    @Test
    void academiaMostraSemanasSequenciaEMelhorMes() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        String gym = api.createTask(token, taskJson("Academia", "MANDATORY",
                List.of("MONDAY", "WEDNESDAY", "FRIDAY"), "18:00", true));

        completeToday(api, token);                                    // segunda: feita
        clock.setTo(monday.plusDays(4).atTime(9, 0), SAO_PAULO);      // quarta passou sem academia
        completeToday(api, token);                                    // sexta: feita
        clock.setTo(monday.plusWeeks(1).atTime(9, 0), SAO_PAULO);     // segunda seguinte, ainda pendente

        String missions = api.getJson(token, "/api/v1/stats/missions");
        assertThat(JsonPath.<List<String>>read(missions, "$[*].name")).containsExactly("Academia");
        assertThat(JsonPath.<Integer>read(missions, "$[0].completed")).isEqualTo(2);
        assertThat(JsonPath.<Integer>read(missions, "$[0].missed")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(missions, "$[0].completionRate")).isEqualTo(66);
        assertThat(JsonPath.<Integer>read(missions, "$[0].currentStreak")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(missions, "$[0].bestStreak")).isEqualTo(1);

        String evolution = api.getJson(token, "/api/v1/stats/missions/" + gym + "?weeks=4");
        assertThat(JsonPath.<List<String>>read(evolution, "$.weeks[*].start"))
                .containsExactly(monday.toString(), monday.plusWeeks(1).toString());
        assertThat(JsonPath.<List<Integer>>read(evolution, "$.weeks[*].totals.done")).containsExactly(2, 0);
        assertThat(JsonPath.<Integer>read(evolution, "$.weeks[0].totals.planned")).isEqualTo(3);
        assertThat(JsonPath.<Integer>read(evolution, "$.bestMonthCompleted")).isPositive();
        assertThat(JsonPath.<Double>read(evolution, "$.weeklyAverage")).isEqualTo(1.0);
        assertThat(JsonPath.<String>read(evolution, "$.firstCompletedDate")).isEqualTo(monday.toString());

        // Missão de outra pessoa não existe
        String intruder = registerUser().accessToken();
        mvc.perform(get("/api/v1/stats/missions/" + gym).header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound());
    }

    private static void completeToday(PlanningApi api, String token) throws Exception {
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, id).andExpect(status().isOk());
    }
}
