package com.gasmtask.shared.storage;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** @param dir diretório raiz dos arquivos enviados (no Docker, um volume) */
@ConfigurationProperties("app.storage")
public record StorageProperties(String dir) {

    public StorageProperties {
        Objects.requireNonNull(dir, "app.storage.dir é obrigatório");
    }
}
