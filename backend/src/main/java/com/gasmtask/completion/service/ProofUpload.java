package com.gasmtask.completion.service;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.storage.ImageType;

import org.springframework.web.multipart.MultipartFile;

/** Imagem recebida e já validada pelo conteúdo (bytes iniciais), não pelo nome ou Content-Type. */
record ProofUpload(byte[] bytes, ImageType type) {

    /** Devolve {@code null} quando nenhum arquivo foi enviado. */
    static ProofUpload from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
        ImageType type = ImageType.detect(bytes).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_IMAGE));
        return new ProofUpload(bytes, type);
    }
}
