package com.gasmtask.gamestate;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** O game-state reflete o que os outros módulos já registram: carteira, streak, coleção, quarto e personagem. */
class GameStateIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private String token;

    @Test
    void juntaOEstadoDeTodosOsModulos() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        token = registerUser().accessToken();

        api.createTask(token, taskJson("Estudar Java", "MANDATORY", List.of("MONDAY"), "21:00", true));
        String occurrence = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, occurrence).andExpect(status().isOk());   // +3 moedas, FIRST_TASK, dia cumprido

        giveCoins(30);
        String mug = buy("mug_coffee");                                // 5
        String cap = buy("cap_red");                                   // 12
        mvc.perform(put("/api/v1/room/items/{id}", mug).header(AUTHORIZATION, bearer(token))).andExpect(status().isOk());
        mvc.perform(put("/api/v1/character/slots/HEAD").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"inventoryItemId\": \"%s\"}".formatted(cap)))
                .andExpect(status().isOk());

        String state = api.getJson(token, "/api/v1/me/game-state");

        assertThat(JsonPath.<String>read(state, "$.timeOfDay")).isEqualTo("MORNING");
        assertThat(JsonPath.<Integer>read(state, "$.coins")).isEqualTo(3 + 30 - 5 - 12);
        assertThat(JsonPath.<Integer>read(state, "$.streak.current")).isEqualTo(1);
        assertThat(JsonPath.<String>read(state, "$.streak.todayStatus")).isEqualTo("FULFILLED");
        assertThat(JsonPath.<Integer>read(state, "$.totals.completedTasks")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(state, "$.totals.achievements")).isEqualTo(1);
        assertThat(JsonPath.<List<String>>read(state, "$.inventory[*].code"))
                .containsExactlyInAnyOrder("mug_coffee", "cap_red");
        assertThat(JsonPath.<List<String>>read(state, "$.room.items[*].code")).containsExactly("mug_coffee");
        assertThat(JsonPath.<List<String>>read(state, "$.room.items[*].assetKey"))
                .containsExactly("decoration.mug_coffee.v1");
        assertThat(JsonPath.<Map<String, Object>>read(state, "$.character.equipped")).containsOnlyKeys("HEAD");
        assertThat(JsonPath.<String>read(state, "$.character.equipped.HEAD.code")).isEqualTo("cap_red");
        // A única tarefa já foi concluída: nada em andamento
        assertThat(JsonPath.<String>read(state, "$.character.state")).isEqualTo("IDLE");
    }

    private void giveCoins(int amount) throws Exception {
        String me = mvc.perform(get("/api/v1/me").header(AUTHORIZATION, bearer(token)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        new JdbcTemplate(dataSource).update(
                "UPDATE wallets SET balance = balance + ?, total_earned = total_earned + ? WHERE user_id = ?",
                amount, amount, UUID.fromString(JsonPath.read(me, "$.id")));
    }

    private String buy(String code) throws Exception {
        String items = mvc.perform(get("/api/v1/store/items").header(AUTHORIZATION, bearer(token)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> ids = JsonPath.read(items, "$[?(@.code == '" + code + "')].id");
        String body = mvc.perform(post("/api/v1/store/items/{id}/purchase", ids.getFirst())
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.inventoryItem.id");
    }
}
