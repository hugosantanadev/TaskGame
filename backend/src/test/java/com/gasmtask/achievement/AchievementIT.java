package com.gasmtask.achievement;

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

/** Conquistas: desbloqueio na conclusão, uma vez só, e progresso na listagem. */
@SuppressWarnings("unused")
class AchievementIT extends IntegrationTest {

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

    private String completeToday(String token) throws Exception {
        String id = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[0].id");
        return api.complete(token, id).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void primeiraConclusaoDesbloqueiaPrimeiroPasso() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Ler", "MANDATORY", ALL_DAYS, null, true));

        List<String> unlocked = JsonPath.read(completeToday(token), "$.unlockedAchievements[*].code");
        assertThat(unlocked).containsExactly("FIRST_TASK");

        String list = api.getJson(token, "/api/v1/achievements");
        List<Boolean> firstTask = JsonPath.read(list, "$[?(@.code == 'FIRST_TASK')].unlocked");
        List<Integer> towardTen = JsonPath.read(list, "$[?(@.code == 'TOTAL_10')].progress");
        assertThat(firstTask).containsExactly(true);
        assertThat(towardTen).containsExactly(1);
    }

    @Test
    void sequenciaDeTresDiasDesbloqueiaNaHoraEUmaVezSo() throws Exception {
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", ALL_DAYS, null, true));

        completeToday(token);
        clock.setTo(monday.plusDays(1).atTime(9, 0), SAO_PAULO);
        completeToday(token);
        clock.setTo(monday.plusDays(2).atTime(9, 0), SAO_PAULO);
        List<String> third = JsonPath.read(completeToday(token), "$.unlockedAchievements[*].code");
        clock.setTo(monday.plusDays(3).atTime(9, 0), SAO_PAULO);
        List<String> fourth = JsonPath.read(completeToday(token), "$.unlockedAchievements[*].code");

        assertThat(third).containsExactly("STREAK_3");
        assertThat(fourth).isEmpty();
    }
}
