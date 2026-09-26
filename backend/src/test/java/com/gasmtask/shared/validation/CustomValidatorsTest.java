package com.gasmtask.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomValidatorsTest {

    private final ValidTimeZoneValidator timeZoneValidator = new ValidTimeZoneValidator();

    @Test
    void acceptsIanaTimeZonesAndNull() {
        assertThat(timeZoneValidator.isValid("America/Sao_Paulo", null)).isTrue();
        assertThat(timeZoneValidator.isValid("America/Recife", null)).isTrue();
        assertThat(timeZoneValidator.isValid(null, null)).isTrue();
    }

    @Test
    void rejectsUnknownTimeZones() {
        assertThat(timeZoneValidator.isValid("Marte/Olympus", null)).isFalse();
        assertThat(timeZoneValidator.isValid("", null)).isFalse();
    }

    @Test
    void countsBytesInsteadOfCharacters() {
        Utf8MaxBytesValidator validator = new Utf8MaxBytesValidator();
        validator.initialize(maxBytes(4));

        assertThat(validator.isValid("abcd", null)).isTrue();
        assertThat(validator.isValid("ação", null)).isFalse(); // 4 caracteres, 6 bytes
        assertThat(validator.isValid(null, null)).isTrue();
    }

    private static Utf8MaxBytes maxBytes(int value) {
        return new Utf8MaxBytes() {
            @Override
            public int value() {
                return value;
            }

            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Utf8MaxBytes.class;
            }
        };
    }
}
