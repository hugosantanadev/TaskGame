package com.gasmtask.stats;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.extraJson;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Estatísticas e resumo semanal: planejado × concluído, dias fechados e o que é parcial ou final. */
class StatsIT extends IntegrationTest {

    private PlanningApi api;
    /** Uma segunda-feira no futuro: o relógio de teste só anda para a frente. */
    private LocalDate monday;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        api = new PlanningApi(mvc);
        monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    /**
     * Segunda: cadastro, "Bíblia" obrigatória (seg, ter e qui, já hoje) concluída e uma extra para terça.
     * Terça: a extra é concluída e a Bíblia, não. O relógio para na quarta às 10h.
     */
    private void playMondayAndTuesday() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", List.of("MONDAY", "TUESDAY", "THURSDAY"), "21:00", true));
        mvc.perform(post("/api/v1/extras").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content(extraJson("Arrumar a mesa", monday.plusDays(1))))
                .andExpect(status().isCreated());
        completeFirstPending();

        clock.setTo(monday.plusDays(1).atTime(10, 0), SAO_PAULO);
        List<String> extras = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[?(@.kind == 'EXTRA')].id");
        api.complete(token, extras.getFirst()).andExpect(status().isOk());

        clock.setTo(monday.plusDays(2).atTime(10, 0), SAO_PAULO);
    }

    @Test
    void resumoParcialMostraDiasFechadosHojeEFuturo() throws Exception {
        playMondayAndTuesday();

        String summary = api.getJson(token, "/api/v1/weeks/" + monday + "/summary");

        assertThat((Boolean) JsonPath.read(summary, "$.finished")).isFalse();
        assertThat(JsonPath.<List<String>>read(summary, "$.days[*].status"))
                .containsExactly("FULFILLED", "FAILED", "REST", "UPCOMING", "UPCOMING", "UPCOMING", "UPCOMING");
        assertThat(JsonPath.<Integer>read(summary, "$.totals.mandatoryPlanned")).isEqualTo(3);
        assertThat(JsonPath.<Integer>read(summary, "$.totals.mandatoryDone")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(summary, "$.totals.extrasDone")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(summary, "$.totals.missed")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(summary, "$.totals.points")).isEqualTo(7);   // 5 da obrigatória + 2 da extra
        assertThat(JsonPath.<Integer>read(summary, "$.totals.coins")).isEqualTo(4);    // 3 + 1, sem bônus
        assertThat(JsonPath.<Integer>read(summary, "$.totals.completionRate")).isEqualTo(50); // 2 de 4
        assertThat(JsonPath.<Integer>read(summary, "$.fulfilledDays")).isEqualTo(1);
        assertThat(JsonPath.<List<String>>read(summary, "$.achievements[*].code")).containsExactly("FIRST_TASK");
        assertThat(JsonPath.<Integer>read(summary, "$.days[1].totals.extrasDone")).isEqualTo(1);
    }

    @Test
    void resumoFicaFinalDepoisDoDomingo() throws Exception {
        playMondayAndTuesday();
        clock.setTo(monday.plusWeeks(1).atTime(8, 0), SAO_PAULO);

        String summary = api.getJson(token, "/api/v1/weeks/" + monday + "/summary");

        assertThat((Boolean) JsonPath.read(summary, "$.finished")).isTrue();
        assertThat(JsonPath.<List<String>>read(summary, "$.days[*].status"))
                .containsExactly("FULFILLED", "FAILED", "REST", "FAILED", "REST", "REST", "REST");
        assertThat(JsonPath.<Integer>read(summary, "$.totals.missed")).isEqualTo(2);
        assertThat(JsonPath.<Integer>read(summary, "$.totals.completionRate")).isEqualTo(50);
    }

    @Test
    void visaoGeralSomaTudoDesdeOCadastro() throws Exception {
        playMondayAndTuesday();

        String overview = api.getJson(token, "/api/v1/stats/overview");

        assertThat(JsonPath.<Integer>read(overview, "$.completedTasks")).isEqualTo(2);
        assertThat(JsonPath.<Integer>read(overview, "$.missedTasks")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(overview, "$.completionRate")).isEqualTo(66);
        assertThat(JsonPath.<Integer>read(overview, "$.points")).isEqualTo(7);
        assertThat(JsonPath.<Integer>read(overview, "$.coinsEarned")).isEqualTo(4);
        assertThat(JsonPath.<Integer>read(overview, "$.coinsSpent")).isZero();
        assertThat(JsonPath.<Integer>read(overview, "$.balance")).isEqualTo(4);
        assertThat(JsonPath.<Integer>read(overview, "$.currentStreak")).isZero();
        assertThat(JsonPath.<Integer>read(overview, "$.longestStreak")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(overview, "$.fulfilledDays")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(overview, "$.failedDays")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(overview, "$.restDays")).isZero();
        assertThat(JsonPath.<Integer>read(overview, "$.achievementsUnlocked")).isEqualTo(1);
        assertThat(JsonPath.<List<String>>read(overview, "$.completedByCategory[*].category"))
                .containsExactlyInAnyOrder("STUDY", "HOME");
    }

    @Test
    void historicoComecaNaSemanaDoCadastroETerminaNaAtual() throws Exception {
        playMondayAndTuesday();
        clock.setTo(monday.plusWeeks(1).plusDays(2).atTime(8, 0), SAO_PAULO);

        String weeks = api.getJson(token, "/api/v1/stats/history?granularity=WEEK&periods=8");

        assertThat(JsonPath.<List<String>>read(weeks, "$.periods[*].start"))
                .containsExactly(monday.toString(), monday.plusWeeks(1).toString());
        assertThat(JsonPath.<List<Boolean>>read(weeks, "$.periods[*].current")).containsExactly(false, true);
        assertThat(JsonPath.<Integer>read(weeks, "$.periods[0].totals.planned")).isEqualTo(4);
        assertThat(JsonPath.<Integer>read(weeks, "$.periods[0].totals.done")).isEqualTo(2);
        // Semana atual: segunda e terça já perdidas, quinta ainda planejada
        assertThat(JsonPath.<Integer>read(weeks, "$.periods[1].totals.mandatoryPlanned")).isEqualTo(3);
        assertThat(JsonPath.<Integer>read(weeks, "$.periods[1].totals.missed")).isEqualTo(2);

        String months = api.getJson(token, "/api/v1/stats/history?granularity=MONTH&periods=3");
        assertThat(JsonPath.<String>read(months, "$.granularity")).isEqualTo("MONTH");
        List<Integer> plannedPerMonth = JsonPath.read(months, "$.periods[*].totals.planned");
        assertThat(plannedPerMonth.stream().mapToInt(Integer::intValue).sum()).isEqualTo(7);
    }

    @Test
    void semanaPrecisaComecarNaSegunda() throws Exception {
        token = registerUser().accessToken();

        mvc.perform(get("/api/v1/weeks/{start}/summary", monday.plusDays(1)).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WEEK_START"));
        mvc.perform(get("/api/v1/stats/history?granularity=YEAR").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private void completeFirstPending() throws Exception {
        String today = mvc.perform(get("/api/v1/today").header(AUTHORIZATION, bearer(token)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(today, "$.occurrences[0].id");
        api.complete(token, id).andExpect(status().isOk());
    }
}
