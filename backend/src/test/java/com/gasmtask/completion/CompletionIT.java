package com.gasmtask.completion;

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

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** Conclusão: recompensa calculada no backend, bônus pagos uma vez, prova validada pelo conteúdo. */
@SuppressWarnings("unused")
class CompletionIT extends IntegrationTest {

    private static final List<String> ALL_DAYS =
            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
    private static final List<String> WEEKDAYS = ALL_DAYS.subList(0, 5);

    private PlanningApi api;
    /** Uma segunda-feira no futuro: o relógio de teste só anda para a frente. */
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        api = new PlanningApi(mvc);
        monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    /** Cadastra na segunda às 9h e avança o relógio; fora do primeiro dia, vale a regra normal do dia congelado. */
    private String userRegisteredOnMondayAt(LocalDateTime now) throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        clock.setTo(now, SAO_PAULO);
        return token;
    }

    @Test
    void concluirNoHorarioComFotoPagaARecompensaCompleta() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Estudar Java", "MANDATORY", List.of("TUESDAY"), "08:00", false, false, 60));
        clock.setTo(monday.plusDays(1).atTime(8, 10), SAO_PAULO);

        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.completeWithProof(token, id, PNG, "image/png")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onTime").value(true))
                .andExpect(jsonPath("$.reward.points").value(5))
                .andExpect(jsonPath("$.reward.baseCoins").value(3))
                .andExpect(jsonPath("$.reward.onTimeBonus").value(1))
                .andExpect(jsonPath("$.reward.proofBonus").value(1))
                .andExpect(jsonPath("$.reward.totalCoins").value(5))
                .andExpect(jsonPath("$.walletBalance").value(5))
                .andExpect(jsonPath("$.day.status").value("FULFILLED"))
                .andExpect(jsonPath("$.streak.current").value(1))
                .andExpect(jsonPath("$.streak.increasedNow").value(true))
                .andExpect(jsonPath("$.occurrence.status").value("COMPLETED"))
                .andExpect(jsonPath("$.occurrence.proofAttached").value(true));

        byte[] stored = mvc.perform(get("/api/v1/occurrences/{id}/proof", id).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(stored).isEqualTo(PNG);

        api.complete(token, id)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OCCURRENCE_NOT_PENDING"));
        List<String> reasons = JsonPath.read(api.getJson(token, "/api/v1/wallet/transactions"), "$.content[*].reason");
        int balance = JsonPath.read(api.getJson(token, "/api/v1/wallet"), "$.balance");
        assertThat(reasons).containsExactlyInAnyOrder("TASK_REWARD", "ON_TIME_BONUS", "PROOF_BONUS");
        assertThat(balance).isEqualTo(5);
    }

    @Test
    void semBonusDeHorarioSeAtrasadaOuPlanejadaNoProprioDia() throws Exception {
        clock.setTo(monday.atTime(7, 40), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", ALL_DAYS, "08:00", true));

        String sameDay = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, sameDay)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onTime").value(false))
                .andExpect(jsonPath("$.reward.totalCoins").value(3));

        clock.setTo(monday.plusDays(1).atTime(11, 0), SAO_PAULO);
        String late = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, late)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onTime").value(false))
                .andExpect(jsonPath("$.walletBalance").value(6));
    }

    @Test
    void missaoComProvaExigeImagemValida() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Academia", "MANDATORY", ALL_DAYS, null, true, true, null));
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");

        api.complete(token, id)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("PROOF_REQUIRED"));
        api.completeWithProof(token, id, "não sou uma imagem".getBytes(StandardCharsets.UTF_8), "image/png")
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
        api.completeWithProof(token, id, JPEG, "image/jpeg")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reward.proofBonus").value(1));
    }

    @Test
    void provaEnviadaDepoisPagaOBonusUmaVez() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Ler", "MANDATORY", ALL_DAYS, null, true));
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, id).andExpect(status().isOk()).andExpect(jsonPath("$.reward.proofBonus").value(0));

        MockMultipartFile photo = new MockMultipartFile("proof", "foto.png", "image/png", PNG);
        mvc.perform(multipart("/api/v1/occurrences/{id}/proof", id).file(photo).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proofBonus").value(1))
                .andExpect(jsonPath("$.walletBalance").value(4))
                .andExpect(jsonPath("$.occurrence.earnedCoins").value(4));
        mvc.perform(multipart("/api/v1/occurrences/{id}/proof", id).file(photo).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROOF_ALREADY_ATTACHED"));
    }

    @Test
    void naoConcluiTarefaDeOutroDia() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Ler", "MANDATORY", List.of("TUESDAY"), null, false));
        String tomorrow = idsOnDay(api.getJson(token, "/api/v1/weeks/current"), 1).getFirst();

        api.complete(token, tomorrow)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("NOT_COMPLETABLE_TODAY"));
    }

    @Test
    void tarefaEFotoDeOutroUsuarioNaoExistem() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String owner = registerUser().accessToken();
        api.createTask(owner, taskJson("Academia", "MANDATORY", List.of("MONDAY"), null, true));
        String id = JsonPath.read(api.getJson(owner, "/api/v1/today"), "$.occurrences[0].id");
        api.completeWithProof(owner, id, PNG, "image/png").andExpect(status().isOk());
        String intruder = registerUser().accessToken();

        // RN30 e RNF11: para quem não é dono, a tarefa e a foto simplesmente não existem
        mvc.perform(get("/api/v1/occurrences/{id}/proof", id).header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(multipart("/api/v1/occurrences/{id}/proof", id)
                        .file(new MockMultipartFile("proof", "foto.png", "image/png", PNG))
                        .header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound());
        api.complete(intruder, id).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/occurrences/{id}", id).header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound());
    }
}
