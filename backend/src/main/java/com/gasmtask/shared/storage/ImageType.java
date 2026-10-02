package com.gasmtask.shared.storage;

import java.util.Optional;

/**
 * Formatos de imagem aceitos como prova. O tipo é detectado pelos primeiros bytes do arquivo,
 * nunca pelo nome nem pelo Content-Type enviado, que o cliente pode falsificar.
 */
public enum ImageType {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extension;

    ImageType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    public static Optional<ImageType> detect(byte[] bytes) {
        if (bytes.length >= 3 && u(bytes[0]) == 0xFF && u(bytes[1]) == 0xD8 && u(bytes[2]) == 0xFF) {
            return Optional.of(JPEG);
        }
        if (bytes.length >= 8 && u(bytes[0]) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return Optional.of(PNG);
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static int u(byte value) {
        return value & 0xFF;
    }
}
