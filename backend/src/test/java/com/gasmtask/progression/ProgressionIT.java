package com.gasmtask.progression;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Elo ranqueado: XP que sobe e desce, promoção, rebaixamento e roupas de elo que ficam para sempre. */
class ProgressionIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private PlanningApi api;
    /** Uma segunda-feira no futuro: o relógio de teste só anda para a frente. */
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
    void concluirRendeXpEODiaCumpridoDaBonus() throws Exception {
        api.createTask(token, taskJson("Estudar", "MANDATORY", List.of("MONDAY"), "21:00", true));
        api.createTask(token, taskJson("Ler", "MANDATORY", List.of("MONDAY"), "22:00", true));
        List<String> ids = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[*].id");

        String first = completed(ids.get(0));
        assertThat(JsonPath.<Integer>read(first, "$.xp.gained")).isEqualTo(5);
        String second = completed(ids.get(1));
        assertThat(JsonPath.<Integer>read(second, "$.xp.gained")).isEqualTo(5 + 10); // tarefa + dia cumprido
        assertThat(JsonPath.<Integer>read(second, "$.xp.status.xp")).isEqualTo(20);
        assertThat(JsonPath.<String>read(second, "$.xp.status.tier")).isEqualTo("IRON");
        assertThat(JsonPath.<Integer>read(second, "$.xp.status.nextRankXp")).isEqualTo(50);
        assertThat((Boolean) JsonPath.read(second, "$.xp.promoted")).isFalse();

        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/today"), "$.rank.xp")).isEqualTo(20);
        String ranking = api.getJson(token, "/api/v1/rankings?size=100"); // padrão: elo (XP)
        assertThat(JsonPath.<String>read(ranking, "$.metric")).isEqualTo("XP");
        assertThat(JsonPath.<Integer>read(ranking, "$.me.value")).isEqualTo(20);
        assertThat(JsonPath.<List<String>>read(ranking, "$.entries.content[?(@.you == true)].rank.tier"))
                .containsExactly("IRON");
    }

    @Test
    void obrigatoriaPerdidaCustaXp() throws Exception {
        api.createTask(token, taskJson("Academia", "MANDATORY", List.of("MONDAY", "TUESDAY"), "18:00", true));
        completed(JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id"));   // 5 + 10

        clock.setTo(monday.plusDays(2).atTime(10, 0), SAO_PAULO); // a terça passou sem a academia
        String rank = api.getJson(token, "/api/v1/me/rank");

        assertThat(JsonPath.<Integer>read(rank, "$.status.xp")).isEqualTo(10);
        assertThat(JsonPath.<Integer>read(rank, "$.recent[0].amount")).isEqualTo(-5);
        assertThat(JsonPath.<String>read(rank, "$.recent[0].reason")).isEqualTo("TASK_MISSED");
        assertThat(JsonPath.<String>read(rank, "$.recent[0].title")).isEqualTo("Academia");
        assertThat(JsonPath.<List<Object>>read(rank, "$.ladder")).hasSize(22);
    }

    @Test
    void subirDeEloEntregaARoupaECairNaoTira() throws Exception {
        api.createTask(token, taskJson("Academia", "MANDATORY", List.of("MONDAY", "TUESDAY"), "18:00", true));
        setXp(160);   // Ferro 3; faltam 15 para o Bronze

        String promotion = completed(JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id"));
        assertThat((Boolean) JsonPath.read(promotion, "$.xp.promoted")).isTrue();
        assertThat(JsonPath.<String>read(promotion, "$.xp.status.tier")).isEqualTo("BRONZE");
        assertThat(JsonPath.<List<String>>read(promotion, "$.xp.unlockedItems[*].code"))
                .containsExactly("rank_bronze_headband");

        // A roupa vai para a coleção de graça, não aparece na loja e pode ser vestida
        String inventory = api.getJson(token, "/api/v1/inventory");
        assertThat(JsonPath.<List<Integer>>read(inventory, "$[?(@.item.code == 'rank_bronze_headband')].pricePaid"))
                .containsExactly(0);
        assertThat(api.getJson(token, "/api/v1/store/items")).doesNotContain("rank_bronze_headband");
        List<String> headband = JsonPath.read(inventory, "$[?(@.item.code == 'rank_bronze_headband')].id");
        mvc.perform(put("/api/v1/character/slots/HEAD").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"inventoryItemId\": \"%s\"}".formatted(headband.getFirst())))
                .andExpect(status().isOk());

        clock.setTo(monday.plusDays(2).atTime(10, 0), SAO_PAULO); // perdeu a terça: 175 - 5 = 170
        String rank = api.getJson(token, "/api/v1/me/rank");
        assertThat(JsonPath.<String>read(rank, "$.status.tier")).isEqualTo("IRON");
        assertThat(JsonPath.<Integer>read(rank, "$.status.division")).isEqualTo(3);
        assertThat(JsonPath.<String>read(rank, "$.peakRank.tier")).isEqualTo("BRONZE");
        assertThat(JsonPath.<List<Boolean>>read(rank, "$.rewards[?(@.tier == 'BRONZE')].item.owned"))
                .containsExactly(true);
        assertThat(JsonPath.<List<Boolean>>read(rank, "$.rewards[?(@.tier == 'SILVER')].item.owned"))
                .containsExactly(false);
        assertThat(api.getJson(token, "/api/v1/inventory")).contains("rank_bronze_headband");
    }

    private String completed(String occurrenceId) throws Exception {
        return api.complete(token, occurrenceId)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** Atalho de teste: põe o XP direto no progresso, sem precisar de semanas de tarefas. */
    private void setXp(int xp) throws Exception {
        String me = api.getJson(token, "/api/v1/me");
        new JdbcTemplate(dataSource).update("UPDATE player_progress SET xp = ?, peak_xp = ? WHERE user_id = ?",
                xp, xp, UUID.fromString(JsonPath.read(me, "$.id")));
    }
}
