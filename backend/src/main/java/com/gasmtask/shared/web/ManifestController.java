package com.gasmtask.shared.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Manifest do PWA servido pela própria API (imagem única) com o tipo certo: o servidor de arquivos não conhece
 * a extensão .webmanifest e mandaria application/octet-stream. Sem o app no classpath, responde 404.
 */
@Controller
class ManifestController {

    private static final MediaType MANIFEST = MediaType.valueOf("application/manifest+json");

    @GetMapping("/manifest.webmanifest")
    ResponseEntity<Resource> manifest() {
        Resource manifest = new ClassPathResource("static/manifest.webmanifest");
        if (!manifest.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).contentType(MANIFEST).body(manifest);
    }
}
