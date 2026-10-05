package com.gasmtask.store;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** Melhorias do quarto: degraus em ordem, aparecem sozinhas no quarto e rendem moedas a mais na categoria. */
class EquipmentIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private String token;

    private void setUpUser(int coins) throws Exception {
        token = registerUser().accessToken();
        String me = json("/api/v1/me");
        UUID userId = UUID.fromString(JsonPath.read(me, "$.id"));
        new JdbcTemplate(dataSource).update(
                "UPDATE wallets SET balance = balance + ?, total_earned = total_earned + ? WHERE user_id = ?",
                coins, coins, userId);
    }

    private String json(String path) throws Exception {
        return mvc.perform(get(path).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String itemId(String code) throws Exception {
        List<String> ids = JsonPath.read(json("/api/v1/store/items?category=EQUIPMENT"), "$[?(@.code == '" + code + "')].id");
        return ids.getFirst();
    }

    @Test
    void degrausSeguemAOrdemDaTrilha() throws Exception {
        setUpUser(1000);

        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("desk_simple")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("EQUIPMENT_LOCKED"));
        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("desk_folding")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated());
        String desk = JsonPath.read(mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("desk_simple"))
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.inventoryItem.id");

        String equipment = json("/api/v1/equipment");
        assertThat(JsonPath.<List<Object>>read(equipment, "$")).hasSize(7);
        assertThat(JsonPath.<List<Integer>>read(equipment, "$[?(@.track == 'DESK')].tier")).containsExactly(2);
        assertThat(JsonPath.<List<Integer>>read(equipment, "$[?(@.track == 'DESK')].coinBonus")).containsExactly(2);
        assertThat(JsonPath.<List<String>>read(equipment, "$[?(@.track == 'DESK')].current.code"))
                .containsExactly("desk_simple");
        assertThat(JsonPath.<List<String>>read(equipment, "$[?(@.track == 'DESK')].next.code"))
                .containsExactly("desk_l_shaped");
        assertThat(JsonPath.<List<Integer>>read(equipment, "$[?(@.track == 'COMPUTER')].tier")).containsExactly(0);
        assertThat(JsonPath.<List<String>>read(equipment, "$[?(@.track == 'COMPUTER')].next.code"))
                .containsExactly("computer_old_laptop");

        // O degrau de baixo já foi superado, e melhoria não é colocada no quarto à mão
        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("desk_folding")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ITEM_ALREADY_OWNED"));
        mvc.perform(put("/api/v1/room/items/{id}", desk).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOR_ROOM"));
        assertThat(JsonPath.<Integer>read(json("/api/v1/me/game-state"), "$.room.equipment.DESK")).isEqualTo(2);
    }

    @Test
    void melhoriaRendeMoedasNasTarefasDaCategoria() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        setUpUser(100);
        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("desk_folding")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated());
        api.createTask(token, taskJson("Estudar", "MANDATORY", List.of("MONDAY"), "18:00", true)); // Estudo → mesa
        String id = JsonPath.<List<String>>read(api.getJson(token, "/api/v1/today"), "$.occurrences[*].id").getFirst();

        String result = api.complete(token, id).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        int base = JsonPath.read(result, "$.reward.baseCoins");
        assertThat(JsonPath.<Integer>read(result, "$.reward.equipmentBonus")).isEqualTo(1);
        assertThat(JsonPath.<Integer>read(result, "$.reward.totalCoins")).isEqualTo(base + 1);
        assertThat(JsonPath.<List<Integer>>read(json("/api/v1/wallet/transactions"),
                "$.content[?(@.reason == 'EQUIPMENT_BONUS')].amount")).containsExactly(1);
    }
}
