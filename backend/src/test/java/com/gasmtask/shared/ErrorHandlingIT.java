package com.gasmtask.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gasmtask.support.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ErrorHandlingIT extends IntegrationTest {

    @Test
    void unknownRouteAnswersWithProblemDetails() throws Exception {
        RegisteredUser user = registerUser();

        mvc.perform(get("/api/v1/rota-que-nao-existe").header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void wrongHttpMethodAnswersWithProblemDetails() throws Exception {
        RegisteredUser user = registerUser();

        // /me aceita GET, PATCH e DELETE (excluir a conta); PUT não existe
        mvc.perform(put(ME).header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void healthCheckIsPublic() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void apiDocsArePublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("GasmTask API"));
    }
}
