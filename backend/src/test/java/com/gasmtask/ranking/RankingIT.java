package com.gasmtask.ranking;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Ranking semanal: ordem, empates, quem fica de fora (oculto ou sem pontos) e a posição de quem consulta.
 * Cada caso roda numa semana distante e só sua, para que só as pessoas dele pontuem nela.
 */
class RankingIT extends IntegrationTest {

    private static final AtomicInteger WEEK = new AtomicInteger(9);

    private PlanningApi api;
    private String suffix;
    private String ana;
    private String bia;
    private String caio;
    private String duda;
    private String eva;

    @BeforeEach
    void setUp() throws Exception {
        api = new PlanningApi(mvc);
        suffix = UUID.randomUUID().toString().substring(0, 8);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY)).plusWeeks(WEEK.getAndIncrement());
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);

        ana = register("Ana");
        bia = register("Bia");
        caio = register("Caio");
        duda = register("Duda");
        eva = register("Eva");
        mvc.perform(patch("/api/v1/me").header(AUTHORIZATION, bearer(duda))
                        .contentType(APPLICATION_JSON).content("{\"rankingVisible\": false}"))
                .andExpect(status().isOk());

        completeToday(ana, 2);   // 10 pontos
        completeToday(bia, 1);   // 5
        completeToday(caio, 1);  // 5, empata com Bia
        completeToday(duda, 3);  // 15, mas oculta
        // Eva não conclui nada
    }

    @Test
    void ordenaPorPontosComEmpateEDeixaDeForaOcultosESemPontos() throws Exception {
        String ranking = api.getJson(ana, "/api/v1/rankings?metric=POINTS&size=100");

        assertThat(JsonPath.<Integer>read(ranking, "$.entries.totalElements")).isEqualTo(3);
        assertThat(JsonPath.<List<String>>read(ranking, "$.entries.content[*].displayName"))
                .containsExactly(name("Ana"), name("Bia"), name("Caio"));
        assertThat(JsonPath.<List<Integer>>read(ranking, "$.entries.content[*].position")).containsExactly(1, 2, 2);
        assertThat(JsonPath.<List<Integer>>read(ranking, "$.entries.content[*].value")).containsExactly(10, 5, 5);
        assertThat(JsonPath.<List<Boolean>>read(ranking, "$.entries.content[*].you")).containsExactly(true, false, false);
        assertThat(JsonPath.<Integer>read(ranking, "$.me.position")).isEqualTo(1);
        // Nada de id: só nome e valor (RNF11)
        assertThat(ranking).doesNotContain("\"id\"").doesNotContain("userId");
    }

    @Test
    void quemSeEscondeVeOndeEstariaEQuemNaoPontuouFicaSemPosicao() throws Exception {
        String hidden = api.getJson(duda, "/api/v1/rankings?metric=POINTS&size=100");
        assertThat(JsonPath.<Integer>read(hidden, "$.me.position")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(hidden, "$.me.value")).isEqualTo(15);
        assertThat((Boolean) JsonPath.read(hidden, "$.me.visible")).isFalse();
        assertThat(JsonPath.<List<String>>read(hidden, "$.entries.content[*].displayName")).doesNotContain(name("Duda"));

        String empty = api.getJson(eva, "/api/v1/rankings?metric=POINTS&size=100");
        assertThat((Object) JsonPath.read(empty, "$.me.position")).isNull();
        assertThat(JsonPath.<Integer>read(empty, "$.me.value")).isZero();
    }

    @Test
    void outrasMetricasEPaginas() throws Exception {
        String tasks = api.getJson(ana, "/api/v1/rankings?metric=COMPLETED_TASKS&size=100");
        assertThat(JsonPath.<List<Integer>>read(tasks, "$.entries.content[*].value")).containsExactly(2, 1, 1);

        String coins = api.getJson(ana, "/api/v1/rankings?metric=COINS_EARNED&size=100");
        assertThat(JsonPath.<List<Integer>>read(coins, "$.entries.content[*].value")).containsExactly(6, 3, 3);

        // A sequência é global (não só da semana): basta conferir a de quem consulta, com hoje cumprido somado
        String streak = api.getJson(ana, "/api/v1/rankings?metric=STREAK&size=100");
        assertThat(JsonPath.<Integer>read(streak, "$.me.value")).isEqualTo(1);
        assertThat(JsonPath.<List<Integer>>read(streak, "$.entries.content[?(@.you == true)].value")).containsExactly(1);

        String second = api.getJson(ana, "/api/v1/rankings?metric=POINTS&page=1&size=1");
        assertThat(JsonPath.<List<String>>read(second, "$.entries.content[*].displayName")).containsExactly(name("Bia"));
        assertThat(JsonPath.<Integer>read(second, "$.entries.totalPages")).isEqualTo(3);

        mvc.perform(get("/api/v1/rankings?metric=LIKES").header(AUTHORIZATION, bearer(ana)))
                .andExpect(status().isBadRequest());
    }

    private String name(String first) {
        return first + " " + suffix;
    }

    private String register(String first) throws Exception {
        MvcResult result = mvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                        .content(registerJson(name(first), first.toLowerCase() + "-" + UUID.randomUUID() + "@gasmtask.test",
                                DEFAULT_PASSWORD, "America/Sao_Paulo")))
                .andExpect(status().isCreated())
                .andReturn();
        return accessTokenOf(result);
    }

    /** Cria {@code count} obrigatórias para hoje (segunda) e conclui todas. */
    private void completeToday(String token, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            api.createTask(token, taskJson("Missão " + i, "MANDATORY", List.of("MONDAY"), "21:00", true));
        }
        List<String> ids = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[*].id");
        for (String id : ids) {
            api.complete(token, id).andExpect(status().isOk());
        }
    }
}
