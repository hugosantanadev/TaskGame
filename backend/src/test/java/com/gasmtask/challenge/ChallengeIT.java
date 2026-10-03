package com.gasmtask.challenge;

import static com.gasmtask.support.PlanningApi.PNG;
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
import org.springframework.test.context.TestPropertySource;

/** Desafios diários: sorteio no primeiro acesso, progresso pelas tarefas e recompensa paga uma vez. */
@TestPropertySource(properties = {"app.challenges.xp-reward=10", "app.challenges.coin-reward=3"})
class ChallengeIT extends IntegrationTest {

    @Test
    void diaCompletoDeManhaCumpreOsTresDesafiosEPagaUmaVezSo() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(8, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        for (int i = 0; i < 4; i++) {
            api.createTask(token, taskJson("Estudo " + i, "MANDATORY", List.of("MONDAY"), "1%d:00".formatted(i), true));
        }

        // Às 8h, com quatro obrigatórias de Estudo criadas hoje, dá para: Madrugador, Registro, Dia completo e
        // Maratona (sem extra, sem três categorias, sem tarefa planejada com antecedência). Três são sorteados.
        String today = api.getJson(token, "/api/v1/today");
        List<String> drawn = JsonPath.read(today, "$.challenges[*].code");
        assertThat(drawn).hasSize(3).isSubsetOf("EARLY_BIRD", "PHOTO", "FULL_DAY", "MARATHON");
        assertThat(JsonPath.<List<Integer>>read(today, "$.challenges[*].progress")).containsOnly(0);
        assertThat(api.getJson(token, "/api/v1/today")).contains(drawn.toArray(String[]::new)); // mesmo sorteio

        List<String> ids = JsonPath.read(today, "$.occurrences[*].id");
        int paid = 0;
        String body = api.completeWithProof(token, ids.getFirst(), PNG, "image/png").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        paid += JsonPath.<List<Object>>read(body, "$.completedChallenges").size();
        String last = body;
        for (String id : ids.subList(1, ids.size())) {
            last = api.complete(token, id).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            paid += JsonPath.<List<Object>>read(last, "$.completedChallenges").size();
        }

        assertThat(paid).isEqualTo(3);
        // XP: 4 tarefas x 5 + 10 do dia cumprido + 3 desafios x 10
        assertThat(JsonPath.<Integer>read(last, "$.xp.status.xp")).isEqualTo(60);
        // Moedas: 4 x 3 + 1 da foto + 3 desafios x 3
        int balance = JsonPath.read(api.getJson(token, "/api/v1/wallet"), "$.balance");
        assertThat(balance).isEqualTo(22);

        String after = api.getJson(token, "/api/v1/today");
        assertThat(JsonPath.<List<Boolean>>read(after, "$.challenges[*].completed")).containsOnly(true);
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/wallet"), "$.balance")).isEqualTo(22);
        List<Integer> rewards = JsonPath.read(api.getJson(token, "/api/v1/wallet/transactions"),
                "$.content[?(@.reason == 'CHALLENGE_REWARD')].amount");
        assertThat(rewards).containsExactly(3, 3, 3);
    }
}
