package com.gasmtask.user;

import static com.gasmtask.support.PlanningApi.taskJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Limite de plano ligado na configuração: o grátis fica com uma missão ativa só. */
@TestPropertySource(properties = "app.plans.free.max-active-missions=1")
class PlanLimitIT extends IntegrationTest {

    @Test
    void planoNoLimiteRecusaMissaoNova() throws Exception {
        String token = registerUser().accessToken();
        new PlanningApi(mvc).createTask(token, taskJson("Academia", "MANDATORY", List.of("MONDAY"), "18:00", false));

        mvc.perform(post("/api/v1/tasks").header(AUTHORIZATION, bearer(token)).contentType(APPLICATION_JSON)
                        .content(taskJson("Estudar", "MANDATORY", List.of("TUESDAY"), "08:00", false)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("PLAN_LIMIT_REACHED"));
    }
}
