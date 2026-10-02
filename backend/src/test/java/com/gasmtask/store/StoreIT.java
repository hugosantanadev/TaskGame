package com.gasmtask.store;

import static com.gasmtask.support.PlanningApi.JPEG;
import static com.gasmtask.support.PlanningApi.PNG;
import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.extraJson;
import static com.gasmtask.support.PlanningApi.idsOnDay;
import static com.gasmtask.support.PlanningApi.taskJson;
import static com.gasmtask.support.PlanningApi.timesOnDay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.IntStream;

import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

/** Loja, inventário, quarto e personagem: compra atômica, item único, regras de uso e isolamento entre usuários. */
@SuppressWarnings("unused")
class StoreIT extends IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private String token;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        token = registerUser().accessToken();
        String me = mvc.perform(get("/api/v1/me").header(AUTHORIZATION, bearer(token)))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        userId = UUID.fromString(JsonPath.read(me, "$.id"));
    }

    /** Atalho de teste: põe moedas direto na carteira, sem precisar concluir tarefas. */
    private void giveCoins(int amount) {
        new JdbcTemplate(dataSource).update(
                "UPDATE wallets SET balance = balance + ?, total_earned = total_earned + ? WHERE user_id = ?",
                amount, amount, userId);
    }

    private String json(String path) throws Exception {
        return mvc.perform(get(path).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String itemId(String code) throws Exception {
        List<String> ids = JsonPath.read(json("/api/v1/store/items"), "$[?(@.code == '" + code + "')].id");
        return ids.getFirst();
    }

    /** Compra e devolve o id do item no inventário. */
    private String buy(String code) throws Exception {
        String body = mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId(code)).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.inventoryItem.id");
    }

    @Test
    void compraDebitaLancaNoExtratoEEntraNaColecao() throws Exception {
        giveCoins(20);

        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("mug_coffee")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletBalance").value(15))
                .andExpect(jsonPath("$.inventoryItem.item.code").value("mug_coffee"))
                .andExpect(jsonPath("$.inventoryItem.pricePaid").value(5));
        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("mug_coffee")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ITEM_ALREADY_OWNED"));

        List<Boolean> owned = JsonPath.read(json("/api/v1/store/items"), "$[?(@.code == 'mug_coffee')].owned");
        List<Integer> purchases = JsonPath.read(json("/api/v1/wallet/transactions"), "$.content[?(@.reason == 'PURCHASE')].amount");
        int spent = JsonPath.read(json("/api/v1/wallet"), "$.totalSpent");
        assertThat(owned).containsExactly(true);
        assertThat(purchases).containsExactly(-5);
        assertThat(spent).isEqualTo(5);
    }

    @Test
    void semSaldoNadaMuda() throws Exception {
        giveCoins(10);

        mvc.perform(post("/api/v1/store/items/{id}/purchase", itemId("aquarium_small")).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_COINS"));

        int balance = JsonPath.read(json("/api/v1/wallet"), "$.balance");
        List<String> inventory = JsonPath.read(json("/api/v1/inventory"), "$[*].id");
        assertThat(balance).isEqualTo(10);
        assertThat(inventory).isEmpty();
    }

    @Test
    void quartoAceitaSoMoveisEDecoracao() throws Exception {
        giveCoins(40);
        String mug = buy("mug_coffee");
        String cap = buy("cap_red");

        mvc.perform(put("/api/v1/room/items/{id}", mug).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item.code").value("mug_coffee"));
        mvc.perform(put("/api/v1/room/items/{id}", mug).header(AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(put("/api/v1/room/items/{id}", cap).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOR_ROOM"));

        List<Boolean> inRoom = JsonPath.read(json("/api/v1/inventory"), "$[?(@.item.code == 'mug_coffee')].inRoom");
        assertThat(inRoom).containsExactly(true);
        mvc.perform(delete("/api/v1/room/items/{id}", mug).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void personagemVesteUmItemPorSlot() throws Exception {
        giveCoins(100);
        String cap = buy("cap_red");
        String headphones = buy("headphones_basic");

        mvc.perform(put("/api/v1/character/slots/HEAD").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"inventoryItemId\": \"%s\"}".formatted(cap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[0].slot").value("HEAD"))
                .andExpect(jsonPath("$.slots[0].item.item.code").value("cap_red"));
        mvc.perform(put("/api/v1/character/slots/HEAD").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"inventoryItemId\": \"%s\"}".formatted(headphones)))
                .andExpect(jsonPath("$.slots[0].item.item.code").value("headphones_basic"));
        mvc.perform(put("/api/v1/character/slots/OUTFIT").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("{\"inventoryItemId\": \"%s\"}".formatted(cap)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOR_SLOT"));

        List<String> equipped = JsonPath.read(json("/api/v1/inventory"), "$[?(@.equippedSlot == 'HEAD')].item.code");
        assertThat(equipped).containsExactly("headphones_basic");
        mvc.perform(delete("/api/v1/character/slots/HEAD").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[0].item").doesNotExist())
                .andExpect(jsonPath("$.state").value("IDLE"));
    }

    @Test
    void itemDeOutroUsuarioNaoExiste() throws Exception {
        giveCoins(10);
        String mug = buy("mug_coffee");
        String intruder = registerUser().accessToken();

        mvc.perform(put("/api/v1/room/items/{id}", mug).header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound());
    }
}
