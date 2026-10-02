package com.gasmtask.shared.web;

import java.util.List;

import org.springframework.data.domain.Page;

/** Página com formato estável no JSON (serializar o Page do Spring Data direto não tem contrato garantido). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }
}
