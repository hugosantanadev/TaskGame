package com.gasmtask.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gasmtask.support.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * PWA servido pela própria API (imagem única): rotas do app abrem o index.html, arquivos com hash ficam em
 * cache longo, e nada disso abre a API. Os arquivos de teste ficam em src/test/resources/static.
 */
class StaticAppIT extends IntegrationTest {

    @Test
    void rotasDoAppAbremOIndexSemLogin() throws Exception {
        mvc.perform(get("/semana"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(header().string("Cache-Control", "no-cache"));
        String index = mvc.perform(get("/perfil/personagem")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(index).contains("<div id=\"root\">");
    }

    @Test
    void arquivosComHashFicamEmCacheEArquivoQueFaltaE404() throws Exception {
        mvc.perform(get("/assets/app-abc123.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"));
        mvc.perform(get("/manifest.webmanifest"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/manifest+json"));
        mvc.perform(get("/assets/nao-existe.js")).andExpect(status().isNotFound());
        mvc.perform(get("/icone-que-falta.png")).andExpect(status().isNotFound());
    }

    @Test
    void aApiContinuaProtegidaEComRespostaDaApi() throws Exception {
        mvc.perform(get("/api/v1/today"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        String token = registerUser().accessToken();
        mvc.perform(get("/api/v1/rota-que-nao-existe").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
