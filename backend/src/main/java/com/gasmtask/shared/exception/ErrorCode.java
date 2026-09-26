package com.gasmtask.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Códigos de erro estáveis da API. O frontend decide a mensagem exibida a partir do {@code code};
 * o texto padrão serve como {@code detail} do Problem Details.
 */
public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Não foi possível ler o corpo da requisição."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "É preciso entrar na sua conta."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Sua sessão terminou. Entre novamente."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Recurso não encontrado."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado neste endereço."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Os dados mudaram enquanto você editava. Tente de novo."),
    PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "O arquivo enviado é grande demais."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de conteúdo não suportado."),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Não foi possível processar a requisição."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado. Tente novamente em instantes.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    /** Código genérico para erros do próprio framework (rota inexistente, método errado etc.). */
    public static ErrorCode fromStatus(int status) {
        return switch (status) {
            case 401 -> UNAUTHENTICATED;
            case 403 -> ACCESS_DENIED;
            case 404 -> RESOURCE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 409 -> CONCURRENT_MODIFICATION;
            case 413 -> PAYLOAD_TOO_LARGE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> status >= 500 ? INTERNAL_ERROR : REQUEST_FAILED;
        };
    }
}
