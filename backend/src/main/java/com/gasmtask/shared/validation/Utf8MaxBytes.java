package com.gasmtask.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Limita o tamanho do texto em bytes UTF-8. Usado na senha: o BCrypt só considera os primeiros 72 bytes,
 * e emojis ou acentos ocupam mais de um byte cada.
 */
@Documented
@Constraint(validatedBy = Utf8MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Utf8MaxBytes {

    int value();

    String message() default "texto longo demais";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
