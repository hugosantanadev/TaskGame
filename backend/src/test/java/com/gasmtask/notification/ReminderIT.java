package com.gasmtask.notification;

import static com.gasmtask.support.PlanningApi.SAO_PAULO;
import static com.gasmtask.support.PlanningApi.taskJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

/** Preferências de lembrete e o cálculo dos próximos lembretes a partir do plano. */
class ReminderIT extends IntegrationTest {

    @Test
    void preferenciasComecamNoPadraoEValidamAAntecedencia() throws Exception {
        String token = registerUser().accessToken();
        PlanningApi api = new PlanningApi(mvc);

        String defaults = api.getJson(token, "/api/v1/me/reminder-settings");
        assertThat((Boolean) JsonPath.read(defaults, "$.tasksEnabled")).isTrue();
        assertThat(JsonPath.<Integer>read(defaults, "$.leadMinutes")).isEqualTo(10);
        assertThat((Object) JsonPath.read(defaults, "$.bedtime")).isNull();

        mvc.perform(put("/api/v1/me/reminder-settings").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"tasksEnabled\": true, \"leadMinutes\": 500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("leadMinutes"));
    }

    @Test
    void proximosLembretesSaemDoPlanoEDasPreferencias() throws Exception {
        PlanningApi api = new PlanningApi(mvc);
        LocalDate monday = LocalDate.now(SAO_PAULO).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        clock.setTo(monday.atTime(9, 0), SAO_PAULO);
        String token = registerUser().accessToken();
        api.createTask(token, taskJson("Bíblia", "MANDATORY", List.of("MONDAY"), "21:00", true));
        api.createTask(token, taskJson("Ler", "MANDATORY", List.of("MONDAY"), null, true)); // sem horário: sem aviso

        mvc.perform(put("/api/v1/me/reminder-settings").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{\"tasksEnabled\": true, \"leadMinutes\": 15, \"bedtime\": \"23:00\", \"wakeTime\": \"06:30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadMinutes").value(15))
                .andExpect(jsonPath("$.bedtime").value("23:00:00"));

        String upcoming = api.getJson(token, "/api/v1/reminders/upcoming?hours=24");

        assertThat(JsonPath.<List<String>>read(upcoming, "$[*].kind")).containsExactly("TASK", "BEDTIME", "WAKE_UP");
        assertThat(JsonPath.<List<String>>read(upcoming, "$[*].notifyAt").stream().map(Instant::parse).toList())
                .containsExactly(
                        monday.atTime(20, 45).atZone(SAO_PAULO).toInstant(),
                        monday.atTime(22, 45).atZone(SAO_PAULO).toInstant(),
                        monday.plusDays(1).atTime(6, 30).atZone(SAO_PAULO).toInstant());
        assertThat(JsonPath.<String>read(upcoming, "$[0].title")).isEqualTo("Bíblia");

        // Concluída, a tarefa sai da lista
        List<String> bible = JsonPath.read(api.getJson(token, "/api/v1/today"), "$.occurrences[?(@.title == 'Bíblia')].id");
        api.complete(token, bible.getFirst()).andExpect(status().isOk());
        assertThat(JsonPath.<List<String>>read(api.getJson(token, "/api/v1/reminders/upcoming"), "$[*].kind"))
                .containsExactly("BEDTIME", "WAKE_UP");
    }
}
