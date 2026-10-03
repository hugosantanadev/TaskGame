package com.gasmtask.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.sql.DataSource;

import com.gasmtask.support.IntegrationTest;
import com.gasmtask.support.PlanningApi;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Perfil dev: a conta demo nasce com histórico de verdade e o seeder não duplica nada se rodar de novo. */
@ActiveProfiles("dev")
class DemoDataIT extends IntegrationTest {

    @Autowired
    private DemoDataSeeder seeder;

    @Autowired
    private DataSource dataSource;

    @Test
    void contaDemoTemHistoricoEloColecaoERivais() throws Exception {
        String login = mvc.perform(post(LOGIN).contentType(APPLICATION_JSON)
                        .content(loginJson("demo@gasmtask.app", "demo1234")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");
        PlanningApi api = new PlanningApi(mvc);

        String overview = api.getJson(token, "/api/v1/stats/overview");
        assertThat(JsonPath.<Integer>read(overview, "$.completedTasks")).isGreaterThan(60);
        assertThat(JsonPath.<Integer>read(overview, "$.missedTasks")).isPositive();
        assertThat(JsonPath.<Integer>read(overview, "$.longestStreak")).isPositive();
        assertThat(JsonPath.<Integer>read(overview, "$.achievementsUnlocked")).isPositive();

        String rank = api.getJson(token, "/api/v1/me/rank");
        assertThat(JsonPath.<String>read(rank, "$.status.tier")).isNotEqualTo("IRON");
        assertThat(JsonPath.<List<Boolean>>read(rank, "$.rewards[?(@.tier == 'BRONZE')].item.owned")).containsExactly(true);

        assertThat(JsonPath.<List<Object>>read(api.getJson(token, "/api/v1/room"), "$.items")).hasSize(5);
        assertThat(JsonPath.<Integer>read(api.getJson(token, "/api/v1/streak"), "$.freezes")).isEqualTo(1);
        assertThat(JsonPath.<List<String>>read(api.getJson(token, "/api/v1/rankings?size=100"),
                "$.entries.content[*].displayName")).contains("Demo", "Ana Souza", "Carla Mendes");

        // Rodar de novo não cria nada
        Integer before = users();
        seeder.run(null);
        assertThat(users()).isEqualTo(before);
    }

    private Integer users() {
        return new JdbcTemplate(dataSource).queryForObject("SELECT count(*) FROM users", Integer.class);
    }
}
