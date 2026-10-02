package com.gasmtask.shared.storage;

/**
 * Armazenamento de arquivos (fotos de prova). Hoje grava em disco; trocar por um bucket S3 ou similar
 * é escrever outra implementação desta interface, sem mexer nas regras.
 */
public interface FileStorage {

    void save(String key, byte[] content);

    byte[] read(String key);

    void delete(String key);
}
