package com.gasmtask.shared.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ImageTypeTest {

    @Test
    void detectaPeloConteudoENaoPeloNome() {
        assertThat(ImageType.detect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).contains(ImageType.JPEG);
        assertThat(ImageType.detect(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10})).contains(ImageType.PNG);
        assertThat(ImageType.detect("RIFF0000WEBPVP8 ".getBytes(StandardCharsets.US_ASCII))).contains(ImageType.WEBP);
        assertThat(ImageType.detect("<script>alert(1)</script>".getBytes(StandardCharsets.US_ASCII))).isEmpty();
        assertThat(ImageType.detect(new byte[0])).isEmpty();
    }
}
