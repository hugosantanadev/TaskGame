package com.gasmtask.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** Aceita apenas identificadores IANA de fuso horário (ex.: America/Sao_Paulo). Nulo é válido. */
@Documented
@Constraint(validatedBy = ValidTimeZoneValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeZone {

    String message() default "fuso horário inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
