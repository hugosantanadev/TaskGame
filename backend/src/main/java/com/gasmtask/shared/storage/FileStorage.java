package com.gasmtask.shared.storage;

/**
 * Armazenamento de arquivos (fotos de prova). Hoje grava em disco; trocar por um bucket S3 ou similar
 * é escrever outra implementação desta interface, sem mexer nas regras.
 */
public interface FileStorage {

    void save(String key, byte[] content);

    byte[] read(String key);

    void delete(String key);

    /** Apaga tudo o que começa com o prefixo (ex.: as fotos de uma conta excluída). */
    void deleteFolder(String prefix);
}
