package com.gasmtask.planning;

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

/** Missões e plano semanal: geração, regra do dia congelado, edição, teto diário e isolamento entre usuários. */
@SuppressWarnings("unused")
class TaskPlanningIT extends IntegrationTest {

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
    void missaoNovaEntraAPartirDeAmanhaOuJaHojeQuandoPedido() throws Exception {
        String token = userRegisteredOnMondayAt(monday.plusDays(1).atTime(7, 0)); // terça

        api.createTask(token, taskJson("Estudar Java", "MANDATORY", WEEKDAYS, "08:00", false));
        String week = api.getJson(token, "/api/v1/weeks/current");
        assertThat(idsOnDay(week, 1)).isEmpty();      // terça, hoje
        assertThat(idsOnDay(week, 2)).hasSize(1);     // quarta
        assertThat(idsOnDay(week, 5)).isEmpty();      // sábado, fora da recorrência
        String next = api.getJson(token, "/api/v1/weeks/next");
        assertThat(IntStream.range(0, 7).map(day -> idsOnDay(next, day).size()).sum()).isEqualTo(5);

        api.createTask(token, taskJson("Bíblia", "MANDATORY", ALL_DAYS, "09:00", true));
        String today = api.getJson(token, "/api/v1/today");
        List<String> titles = JsonPath.read(today, "$.occurrences[*].title");
        int planned = JsonPath.read(today, "$.progress.mandatoryPlanned");
        assertThat(titles).containsExactly("Bíblia");
        assertThat(planned).isEqualTo(1);
    }

    @Test
    void hojeAceitaInclusoesMasNaoRemocoes() throws Exception {
        LocalDate wednesday = monday.plusDays(2);
        String token = userRegisteredOnMondayAt(wednesday.atTime(10, 0));
        String gym = api.createTask(token, taskJson("Academia", "MANDATORY", List.of("WEDNESDAY", "FRIDAY"), "18:00", true));

        String today = api.getJson(token, "/api/v1/today");
        String todayId = JsonPath.read(today, "$.occurrences[0].id");
        boolean removable = JsonPath.read(today, "$.occurrences[0].removable");
        assertThat(removable).isFalse();

        mvc.perform(delete("/api/v1/occurrences/{id}", todayId).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("DAY_LOCKED"));
        // Mudar o horário de hoje também é tirar a tarefa do lugar (e abriria brecha para o bônus de horário)
        mvc.perform(patch("/api/v1/occurrences/{id}", todayId).header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"date\": \"%s\", \"time\": \"20:00\"}".formatted(wednesday)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("DAY_LOCKED"));

        String fridayId = idsOnDay(api.getJson(token, "/api/v1/weeks/current"), 4).getFirst();
        mvc.perform(delete("/api/v1/occurrences/{id}", fridayId).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        // Incluir em hoje vale; a mesma missão duas vezes no dia, não
        String reading = api.createTask(token, taskJson("Ler", "MANDATORY", List.of("FRIDAY"), null, false));
        mvc.perform(post("/api/v1/weeks/{start}/occurrences", monday).header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"taskId\": \"%s\", \"date\": \"%s\"}".formatted(reading, wednesday)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.removable").value(false));
        mvc.perform(post("/api/v1/weeks/{start}/occurrences", monday).header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"taskId\": \"%s\", \"date\": \"%s\"}".formatted(gym, wednesday)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OCCURRENCE_ALREADY_ON_DATE"));

        // Extras: hoje não, amanhã sim
        mvc.perform(post("/api/v1/extras").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content(extraJson("Arrumar a mesa", wednesday)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("DAY_LOCKED"));
        mvc.perform(post("/api/v1/extras").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content(extraJson("Arrumar a mesa", wednesday.plusDays(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kind").value("EXTRA"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.coins").value(1));
    }

    @Test
    void edicaoValeAPartirDeAmanhaEArquivarTiraDoPlano() throws Exception {
        String token = userRegisteredOnMondayAt(monday.plusDays(1).atTime(7, 0)); // terça
        String id = api.createTask(token, taskJson("Estudar Java", "MANDATORY", ALL_DAYS, "08:00", true));

        mvc.perform(put("/api/v1/tasks/{id}", id).header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(taskJson("Estudar Java", "MANDATORY", ALL_DAYS, "10:00", false)))
                .andExpect(status().isOk());
        String week = api.getJson(token, "/api/v1/weeks/current");
        assertThat(timesOnDay(week, 1).getFirst()).startsWith("08:00"); // hoje fica como estava
        assertThat(timesOnDay(week, 2).getFirst()).startsWith("10:00"); // amanhã já muda

        mvc.perform(post("/api/v1/tasks/{id}/archive", id).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
        String after = api.getJson(token, "/api/v1/weeks/current");
        assertThat(idsOnDay(after, 1)).hasSize(1);
        assertThat(idsOnDay(after, 2)).isEmpty();
        List<String> archived = JsonPath.read(api.getJson(token, "/api/v1/tasks?archived=true"), "$[*].id");
        assertThat(archived).containsExactly(id);
    }

    @Test
    void tetoDeDezObrigatoriasPorDia() throws Exception {
        String token = userRegisteredOnMondayAt(monday.plusDays(1).atTime(7, 0));
        for (int i = 1; i <= 10; i++) {
            api.createTask(token, taskJson("Missão " + i, "MANDATORY", List.of("SATURDAY"), null, false));
        }

        mvc.perform(post("/api/v1/tasks").header(AUTHORIZATION, bearer(token)).contentType(APPLICATION_JSON)
                        .content(taskJson("Missão 11", "MANDATORY", List.of("SATURDAY"), null, false)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("DAILY_LIMIT_REACHED"));
    }

    @Test
    void primeiroDiaFicaEditavelAteAPrimeiraConclusao() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Ler", "MANDATORY", ALL_DAYS, "20:00", true));
        String first = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        mvc.perform(delete("/api/v1/occurrences/{id}", first).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        api.createTask(token, taskJson("Bíblia", "MANDATORY", ALL_DAYS, "09:00", true));
        api.createTask(token, taskJson("Alongar", "MANDATORY", ALL_DAYS, null, true));
        List<String> ids = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[*].id");
        api.complete(token, ids.get(0)).andExpect(status().isOk());

        mvc.perform(delete("/api/v1/occurrences/{id}", ids.get(1)).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().is(422));
    }

    @Test
    void tarefaDeOutroUsuarioNaoExiste() throws Exception {
        String owner = userRegisteredOnMondayAt(monday.plusDays(1).atTime(7, 0));
        api.createTask(owner, taskJson("Estudar Java", "MANDATORY", ALL_DAYS, "08:00", false));
        String id = idsOnDay(api.getJson(owner, "/api/v1/weeks/current"), 2).getFirst();
        String intruder = registerUser().accessToken();

        mvc.perform(delete("/api/v1/occurrences/{id}", id).header(AUTHORIZATION, bearer(intruder)))
                .andExpect(status().isNotFound());
        api.complete(intruder, id).andExpect(status().isNotFound());
    }

    @Test
    void sugereDiasParaVezesPorSemana() throws Exception {
        String token = registerUser().accessToken();

        List<String> days = JsonPath.read(api.getJson(token, "/api/v1/tasks/schedule-suggestion?timesPerWeek=3"), "$.days");

        assertThat(days).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }
}
