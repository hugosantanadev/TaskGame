package com.gasmtask.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Atalhos de API para os testes da Fase 2 (missões, plano, conclusão). */
public final class PlanningApi {

    public static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    /** Basta a assinatura: o backend detecta o formato pelos bytes iniciais. */
    public static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    public static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};

    private final MockMvc mvc;

    public PlanningApi(MockMvc mvc) {
        this.mvc = mvc;
    }

    public static String taskJson(String name, String kind, List<String> days, String time, boolean startToday) {
        return taskJson(name, kind, days, time, startToday, false, null);
    }

    public static String taskJson(String name, String kind, List<String> days, String time, boolean startToday,
                                  boolean requiresProof, Integer durationMinutes) {
        String schedule = days.stream()
                .map(day -> time == null
                        ? "{\"dayOfWeek\": \"%s\"}".formatted(day)
                        : "{\"dayOfWeek\": \"%s\", \"time\": \"%s\"}".formatted(day, time))
                .collect(Collectors.joining(", "));
        return """
                {"name": "%s", "category": "STUDY", "kind": "%s", "points": %s, "durationMinutes": %s,
                 "requiresProof": %s, "schedule": [%s], "startToday": %s}
                """.formatted(name, kind, "EXTRA".equals(kind) ? "2" : "null",
                durationMinutes == null ? "null" : durationMinutes, requiresProof, schedule, startToday);
    }

    public static String extraJson(String title, LocalDate date) {
        return """
                {"title": "%s", "category": "HOME", "points": 2, "date": "%s"}
                """.formatted(title, date);
    }

    public String createTask(String token, String json) throws Exception {
        String body = mvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.id");
    }

    public String getJson(String token, String path) throws Exception {
        return mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    public ResultActions complete(String token, String occurrenceId) throws Exception {
        return mvc.perform(post("/api/v1/occurrences/{id}/complete", occurrenceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    public ResultActions completeWithProof(String token, String occurrenceId, byte[] image, String contentType)
            throws Exception {
        return mvc.perform(multipart("/api/v1/occurrences/{id}/complete", occurrenceId)
                .file(new MockMultipartFile("proof", "foto", contentType, image))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    /** Ids das tarefas de um dia da semana (0 = segunda) no JSON de /weeks. */
    public static List<String> idsOnDay(String weekJson, int dayIndex) {
        return JsonPath.read(weekJson, "$.days[" + dayIndex + "].occurrences[*].id");
    }

    public static List<String> timesOnDay(String weekJson, int dayIndex) {
        return JsonPath.read(weekJson, "$.days[" + dayIndex + "].occurrences[*].plannedTime");
    }
}
