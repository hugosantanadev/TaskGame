package com.gasmtask.streak;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

/** Protetor de sequência: compra com moedas, limite e o dia de falha que ele salva na virada. */
class StreakFreezeIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private PlanningApi api;
    private LocalDate monday;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        api = new PlanningApi(mvc);
        monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        token = registerUser().accessToken();
    }

    @Test
    void protetorSalvaODiaDeFalhaEMantemASequencia() throws Exception {
        api.createTask(token, taskJson("Academia", "MANDATORY", List.of("MONDAY", "TUESDAY"), "18:00", true));
        String monday0 = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, monday0).andExpect(status().isOk());   // segunda cumprida: sequência 1
        giveCoins(50);

        buyFreeze().andExpect(status().isOk())
                .andExpect(jsonPath("$.freezes").value(1))
                .andExpect(jsonPath("$.maxFreezes").value(2))
                .andExpect(jsonPath("$.freezePrice").value(40));
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/wallet"), "$.balance")).isEqualTo(3 + 50 - 40);
        assertThat(JsonPath.<List<Integer>>read(api.getJson(token, "/api/v1/wallet/transactions"),
                "$.content[?(@.reason == 'STREAK_FREEZE')].amount")).containsExactly(-40);

        clock.setTo(monday.plusDays(2).atTime(10, 0), SAO_PAULO);   // a terça passou sem a academia
        String streak = api.getJson(token, "/api/v1/streak");
        assertThat(JsonPath.<Integer>read(streak, "$.current")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(streak, "$.freezes")).isZero();
        assertThat(JsonPath.<String>read(streak, "$.lastFrozenDate")).isEqualTo(monday.plusDays(1).toString());

        String summary = api.getJson(token, "/api/v1/weeks/" + monday + "/summary");
        assertThat(JsonPath.<String>read(summary, "$.days[1].status")).isEqualTo("FROZEN");
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/stats/overview"), "$.frozenDays")).isEqualTo(1);
        // O protetor salva a sequência, não o XP: a obrigatória perdida ainda custa
        assertThat(JsonPath.<String>read(api.getJson(token, "/api/v1/me/rank"), "$.recent[0].reason"))
                .isEqualTo("TASK_MISSED");
    }

    @Test
    void compraTemLimiteEPrecisaDeSaldo() throws Exception {
        buyFreeze().andExpect(status().is(422)).andExpect(jsonPath("$.code").value("INSUFFICIENT_COINS"));

        giveCoins(200);
        buyFreeze().andExpect(status().isOk());
        buyFreeze().andExpect(status().isOk()).andExpect(jsonPath("$.freezes").value(2));
        buyFreeze().andExpect(status().is(422)).andExpect(jsonPath("$.code").value("FREEZE_LIMIT_REACHED"));
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/wallet"), "$.balance")).isEqualTo(200 - 80);
    }

    private ResultActions buyFreeze() throws Exception {
        return mvc.perform(post("/api/v1/streak/freezes").header(AUTHORIZATION, bearer(token)));
    }

    private void giveCoins(int amount) throws Exception {
        UUID userId = UUID.fromString(JsonPath.read(api.getJson(token, "/api/v1/me"), "$.id"));
        new JdbcTemplate(dataSource).update(
                "UPDATE wallets SET balance = balance + ?, total_earned = total_earned + ? WHERE user_id = ?",
                amount, amount, userId);
    }
}
