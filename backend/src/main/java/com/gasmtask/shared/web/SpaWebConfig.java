package com.gasmtask.shared.web;

import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serve o PWA quando ele vem dentro da imagem (Dockerfile da raiz, em {@code classpath:/static}): API e app na
 * mesma origem, como o Nginx faz no Docker Compose. Sem o app no classpath (desenvolvimento e testes), nada muda.
 * <ul>
 *   <li>arquivos com hash no nome ({@code /assets}) ficam em cache por um ano;</li>
 *   <li>o resto (index.html, service worker, manifest) é sempre revalidado: é assim que uma versão nova chega;</li>
 *   <li>rotas do app sem arquivo (ex.: {@code /semana}) abrem o index.html e o React Router decide a tela;
 *       rotas da API nunca caem aqui (os controllers vêm antes) e arquivo que falta continua 404.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class SpaWebConfig implements WebMvcConfigurer {

    private static final String STATIC = "classpath:/static/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(STATIC + "assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        registry.addResourceHandler("/**")
                .addResourceLocations(STATIC)
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new SpaFallbackResolver());
    }

    /** Devolve o arquivo pedido; para uma rota do app (sem extensão), o index.html. */
    static class SpaFallbackResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
                return requested;
            }
            boolean appRoute = !resourcePath.startsWith("api/") && !resourcePath.contains(".");
            Resource index = new ClassPathResource("static/index.html");
            return appRoute && index.exists() ? index : null;
        }
    }
}
