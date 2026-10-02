package com.gasmtask.support;

import java.io.IOException;

import javax.sql.DataSource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * PostgreSQL real para os testes de integração, sem Docker: os binários oficiais (na mesma versão maior do
 * Docker Compose) rodam como um processo local numa pasta temporária, criada a cada execução da suíte.
 * O DataSource definido aqui substitui o da configuração automática; o Flyway roda as migrations nele.
 */
@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresConfig {

    @Bean(destroyMethod = "close")
    EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.start();
    }

    @Bean
    DataSource dataSource(EmbeddedPostgres postgres) {
        return DataSourceBuilder.create()
                .url(postgres.getJdbcUrl("postgres", "postgres"))
                .username("postgres")
                .build();
    }
}
