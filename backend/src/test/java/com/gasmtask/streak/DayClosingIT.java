package com.gasmtask.streak;

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

/** Virada do dia com relógio controlado: pendentes viram perdidas, descanso mantém o streak, falha zera. */
@SuppressWarnings("unused")
class DayClosingIT extends IntegrationTest {

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
    void falhaZeraOStreakEDescansoMantem() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", List.of("MONDAY", "TUESDAY", "THURSDAY"), "21:00", true));

        completeToday(token);                                      // segunda: cumprido
        clock.setTo(monday.plusDays(1).atTime(10, 0), SAO_PAULO);
        String tuesday = api.getJson(token, "/api/v1/today");
        int afterMonday = JsonPath.read(tuesday, "$.streak.current");
        String tuesdayStatus = JsonPath.read(tuesday, "$.streak.todayStatus");
        assertThat(afterMonday).isEqualTo(1);
        assertThat(tuesdayStatus).isEqualTo("PENDING");
        completeToday(token);                                      // terça: cumprido

        clock.setTo(monday.plusDays(2).atTime(10, 0), SAO_PAULO);  // quarta: sem obrigatória
        String wednesday = api.getJson(token, "/api/v1/today");
        int onRestDay = JsonPath.read(wednesday, "$.streak.current");
        String restStatus = JsonPath.read(wednesday, "$.streak.todayStatus");
        assertThat(onRestDay).isEqualTo(2);
        assertThat(restStatus).isEqualTo("REST");

        clock.setTo(monday.plusDays(4).atTime(10, 0), SAO_PAULO);  // sexta: a quinta passou sem conclusão
        String streak = api.getJson(token, "/api/v1/streak");
        int current = JsonPath.read(streak, "$.current");
        int longest = JsonPath.read(streak, "$.longest");
        assertThat(current).isZero();
        assertThat(longest).isEqualTo(2);
        List<String> thursday = JsonPath.read(api.getJson(token, "/api/v1/weeks/current"), "$.days[3].occurrences[*].status");
        assertThat(thursday).containsExactly("MISSED");
    }

    private void completeToday(String token) throws Exception {
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        api.complete(token, id).andExpect(status().isOk());
    }
}
