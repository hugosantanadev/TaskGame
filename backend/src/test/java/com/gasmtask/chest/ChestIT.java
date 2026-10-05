package com.gasmtask.chest;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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

/** Baú semanal: nasce na semana seguinte do tamanho dos dias cumpridos e paga uma vez ao abrir. */
class ChestIT extends IntegrationTest {

    private static final List<String> ALL_DAYS =
            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private PlanningApi api;
    private LocalDate monday;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        api = new PlanningApi(mvc);
        monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", ALL_DAYS, "21:00", true));
    }

    @Test
    void semanaPerfeitaDaBauLendarioQuePagaUmaVez() throws Exception {
        for (int day = 0; day < 7; day++) {
            clock.setTo(monday.plusDays(day).atTime(9, 0), SAO_PAULO);
            completeToday();
        }
        clock.setTo(monday.plusWeeks(1).atTime(8, 0), SAO_PAULO);
        int before = JsonPath.read(api.getJson(token, "/api/v1/wallet"), "$.balance");

        String today = api.getJson(token, "/api/v1/today");
        assertThat(JsonPath.<String>read(today, "$.pendingChest.tier")).isEqualTo("LEGENDARY");
        assertThat(JsonPath.<Integer>read(today, "$.pendingChest.fulfilledDays")).isEqualTo(7);
        assertThat((Boolean) JsonPath.read(today, "$.pendingChest.hasItem")).isTrue();
        String chestId = JsonPath.read(today, "$.pendingChest.id");

        String opened = mvc.perform(post("/api/v1/chests/{id}/open", chestId).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(JsonPath.<Integer>read(opened, "$.coins")).isEqualTo(80);
        assertThat(JsonPath.<Integer>read(opened, "$.xp.gained")).isEqualTo(80);
        String item = JsonPath.read(opened, "$.item.code");
        assertThat(JsonPath.<Integer>read(opened, "$.walletBalance")).isEqualTo(before + 80);
        assertThat(api.getJson(token, "/api/v1/inventory")).contains(item);

        mvc.perform(post("/api/v1/chests/{id}/open", chestId).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHEST_ALREADY_OPENED"));
        assertThat((Object) JsonPath.read(api.getJson(token, "/api/v1/today"), "$.pendingChest")).isNull();
        assertThat(JsonPath.<List<Boolean>>read(api.getJson(token, "/api/v1/chests"), "$[*].opened"))
                .containsExactly(true);
    }

    @Test
    void umDiaSoNaoDaBauEOutraPessoaNaoAbreOBauAlheio() throws Exception {
        completeToday();   // só a segunda
        clock.setTo(monday.plusWeeks(1).atTime(8, 0), SAO_PAULO);

        assertThat((Object) JsonPath.read(api.getJson(token, "/api/v1/today"), "$.pendingChest")).isNull();
        assertThat(JsonPath.<List<Object>>read(api.getJson(token, "/api/v1/chests"), "$")).isEmpty();
    }

    private void completeToday() throws Exception {
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, id).andExpect(status().isOk());
    }
}
